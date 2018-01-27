package eu.kyngas.grapes.music.util;

import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.IO;
import eu.kyngas.grapes.common.util.N;
import eu.kyngas.grapes.common.util.ThreadUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Setter(AccessLevel.PRIVATE)
public class AsyncInputStream implements ReadStream<Buffer>, Closeable {
  private static final int DEFAULT_CHUNK_SIZE = 8192;
  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  private final Vertx vertx;
  private final Executor executor;
  private final PushbackInputStream in;
  private final int chunkSize;
  private final long sleepBetweenReadsMillis;

  private Handler<Buffer> readHandler;
  private Handler<Void> endHandler;
  private Handler<Throwable> failureHandler;

  private Status status = Status.ACTIVE;

  @Getter
  private long totalBytesRead;

  public AsyncInputStream(InputStream in) {
    this(Ctx.vertx(), Executors.newSingleThreadExecutor(), in, DEFAULT_CHUNK_SIZE, 20L);
  }

  public AsyncInputStream(Vertx vertx, Executor executor, InputStream in, int chunkSize, long sleepBetweenReadsMillis) {
    if (N.anyNull(vertx, executor, in)) {
      throw new NullPointerException("Arguments cannot be null");
    }
    if (chunkSize <= 0) {
      throw new IllegalArgumentException(String.format("chunkSize: %s (expected > 0)", chunkSize));
    }
    if (sleepBetweenReadsMillis < 0) {
      throw new IllegalArgumentException(String.format("sleepBetweenReadsMillis: %s (expected >= 0)",
          sleepBetweenReadsMillis));
    }
    this.vertx = vertx;
    this.executor = executor;
    this.in = in instanceof PushbackInputStream ? (PushbackInputStream) in : new PushbackInputStream(in);
    this.chunkSize = chunkSize;
    this.sleepBetweenReadsMillis = sleepBetweenReadsMillis;
  }

  @Override
  public ReadStream<Buffer> handler(Handler<Buffer> handler) {
    N.safe(handler, h -> {
      setReadHandler(h);
      doRead();
    });
    return this;
  }

  @Override
  public ReadStream<Buffer> endHandler(Handler<Void> handler) {
    N.safe(handler, this::setEndHandler);
    return this;
  }

  @Override
  public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
    N.safe(handler, this::setFailureHandler);
    return this;
  }

  @Override
  public ReadStream<Buffer> pause() {
    C.ifTrue(isActive(), () -> setStatus(Status.PAUSED));
    return this;
  }

  @Override
  public ReadStream<Buffer> resume() {
    C.ifTrue(isClosed(), () -> {
      throw new IllegalArgumentException("Cannot resume a closed stream");
    });
    C.ifTrue(isPaused(), () -> {
      setStatus(Status.ACTIVE);
      doRead();
    });
    return this;
  }

  @Override
  public void close() {
    setStatus(Status.CLOSED);
    vertx.runOnContext(v -> N.safe(endHandler, h -> h.handle(null)));
  }

  public enum Status {
    ACTIVE, PAUSED, CLOSED
  }

  public boolean isActive() {
    return status == Status.ACTIVE;
  }

  public boolean isPaused() {
    return status == Status.PAUSED;
  }

  public boolean isClosed() {
    return status == Status.CLOSED;
  }

  private void doRead() {
    C.ifTrue(isActive(), () -> executor.execute(() -> {
      try {
        byte[] buffer = readChunk();
        C.check(buffer == null || buffer.length == 0, this::close, () -> {
          vertx.runOnContext(v -> {
            readHandler.handle(Buffer.buffer(buffer));
            doRead();
          });
          C.ifTrue(sleepBetweenReadsMillis > 0, () -> ThreadUtil.sleep(sleepBetweenReadsMillis));
        });
      } catch (Exception e) {
        setStatus(Status.CLOSED);
        IO.close(in);
        vertx.runOnContext(v -> N.safe(failureHandler, h -> h.handle(e)));
      }
    }));
  }

  private byte[] readChunk() throws Exception {
    // TODO: 27.01.2018 audioStream does not support unreading, remove this + pushbackInputStream wrapper?
/*    if (isEndOfInput()) {
      return EMPTY_BYTE_ARRAY;
    }*/
    int bytesAvailable = in.available();
    int chunkSize = bytesAvailable <= 0 ? this.chunkSize : Math.min(this.chunkSize, in.available());
    try {
      byte[] buffer = new byte[chunkSize];
      int bytesRead = in.read(buffer);
      if (bytesRead <= 0) {
        return null;
      }
      totalBytesRead += bytesRead;
      return buffer;
    } catch (IOException e) {
      IO.close(in);
      return null;
    }
  }

  private boolean isEndOfInput() throws IOException {
    int bytesRead = in.read();
    if (bytesRead < 0) {
      return true;
    }
    in.unread(bytesRead);
    return false;
  }
}
