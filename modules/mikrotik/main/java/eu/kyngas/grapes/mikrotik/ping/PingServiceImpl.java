/*
 * Copyright (C) 2018 Kristjan Hendrik Küngas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kyngas.grapes.mikrotik.ping;

import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.N;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.Setter;
import eu.kyngas.grapes.mikrotik.router.RouterService;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Setter(AccessLevel.PRIVATE)
public class PingServiceImpl implements PingService {
  private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
  private static final int MAX_RETRIES = 5;

  private final Vertx vertx;
  private final String pingHost;
  private final RouterService routerService;
  private final ExecutorService executor;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final AtomicReference<Process> pingProcess = new AtomicReference<>();

  private Handler<Ping> successHandler;
  private Handler<Ping> failHandler;
  private Handler<Ping> disconnectedHandler;
  private Handler<Ping> connectedHandler;
  private int tries;
  private boolean connected = true;

  public PingServiceImpl(JsonObject config) {
    if (!IS_WINDOWS) {
      throw new IllegalArgumentException("Unsupported platform");
    }
    this.vertx = Ctx.vertx();
    this.pingHost = config.getString("ping-host");
    this.executor = Executors.newSingleThreadExecutor();
    this.routerService = RouterService.create();
    this.executor.execute(startPinging());
  }
  
  private Runnable startPinging() {
    return () -> {
      try {
        pingProcess.set(new ProcessBuilder("ping", pingHost, "-t").start());
        BufferedReader reader = new BufferedReader(new InputStreamReader(pingProcess.get().getInputStream()));
        String line;
        while (running.get() && (line = reader.readLine()) != null) {
          String currentLine = line;
          vertx.runOnContext(v -> logic(parse(currentLine)));
        }
        Logs.debug("Pinging stopped.");
        reader.close();
      } catch (IOException e) {
        Logs.error("Failed to start Ping process.", e);
      }
    };
  }

  private Ping parse(String input) {
    if (input == null || input.trim().isEmpty() || input.startsWith("Pinging")) {
      return null;
    }
    String[] parts = input.split(": ");
    if (parts.length == 1 && input.equals("Request timed out.")) {
      return Ping.timedOut(pingHost);
    }
    if (parts[1].contains("unreachable")) {
      return Ping.unreachable(parts[0].split(" ")[2]);
    }
    if (parts[1].contains("bytes")) {
      String host = parts[0].split(" ")[2];
      String time = parts[1].split(" ")[1].split("=")[1].replace("ms", "");
      return new Ping(host, Integer.parseInt(time), true, null);
    }
    return new Ping(null, -1, false, "Unknown state");
  }

  private void logic(Ping ping) {
    if (ping == null) {
      return;
    }
    if (ping.isSuccess()) {
      C.ifFalse(connected, () -> N.safe(connectedHandler, h -> h.handle(ping)));
      N.safe(successHandler, h -> h.handle(ping));
      tries = 0;
      connected = true;
    } else {
      tries++;
      N.safe(failHandler, h -> h.handle(ping));
    }
    if (tries >= MAX_RETRIES && connected) {
      N.safe(disconnectedHandler, h -> h.handle(ping));
      connected = false;
      routerService.restartLte(ar -> {
        C.ifTrue(ar.failed(), () -> Logs.error("Failed to restart LTE.", ar.cause()));
        tries = 0;
      });
    }
  }

  @Override
  public void pingSuccess(Handler<Ping> handler) {
    N.safe(handler, this::setSuccessHandler);
  }

  @Override
  public void pingFail(Handler<Ping> handler) {
    N.safe(handler, this::setFailHandler);
  }

  @Override
  public void disconnected(Handler<Ping> handler) {
    N.safe(handler, this::setDisconnectedHandler);
  }

  @Override
  public void connected(Handler<Ping> handler) {
    N.safe(handler, this::setConnectedHandler);
  }

  @Override
  public void close(Handler<Void> handler) {
    running.set(false);
    pingProcess.get().destroy();
    executor.shutdown();
    routerService.close(handler);
  }
}
