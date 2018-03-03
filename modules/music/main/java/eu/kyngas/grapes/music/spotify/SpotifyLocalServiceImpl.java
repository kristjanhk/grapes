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

package eu.kyngas.grapes.music.spotify;

import eu.kyngas.grapes.common.service.ProxyServiceImpl;
import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.Config;
import eu.kyngas.grapes.common.util.F;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.Networks;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class SpotifyLocalServiceImpl extends ProxyServiceImpl<SpotifyLocalService> implements SpotifyLocalService {
  private static final String DBUS_SERVICE_SPOTIFY = "org.mpris.MediaPlayer2.spotify";
  private static final String DBUS_SERVICE_PLAYER = "org.mpris.MediaPlayer2.Player";
  private static final String DBUS_MPRIS_PLAYER = "/org/mpris/MediaPlayer2";
  private static final String DBUS_ADDRESS_LOCATION = "/tmp/dbus-address";

  private final String dbusAddress = getDBusAddressFromFile();
  private DBusConnection conn;
  private Player player;

  SpotifyLocalServiceImpl() {
    super(ADDRESS, SpotifyLocalService.class);
  }

  private <T> void checkConnection(Handler<AsyncResult<T>> handler, Runnable ifConnected) {
    if (!Networks.getInstance().isProduction()) {
      handler.handle(F.fail("Spotify cannot be controlled in a development environment."));
      return;
    }
    if (dbusAddress == null || dbusAddress.isEmpty()) {
      handler.handle(F.fail("Could not get DBus address."));
      return;
    }
    if (!connect()) {
      handler.handle(F.fail("Connection to DBus has failed."));
      return;
    }
    if (!getPlayer()) {
      handler.handle(F.fail("Spotify remote object is invalid."));
      return;
    }
    handler.handle(F.success());
    ifConnected.run();
  }

  @Override
  public SpotifyLocalService play(Handler<AsyncResult<Void>> handler) {
    checkConnection(handler, () -> player.Play());
    return this;
  }

  @Override
  public SpotifyLocalService pause(Handler<AsyncResult<Void>> handler) {
    checkConnection(handler, () -> player.Stop());
    return this;
  }

  @Override
  public SpotifyLocalService previous(Handler<AsyncResult<Void>> handler) {
    checkConnection(handler, () -> player.Previous());
    return this;
  }

  @Override
  public SpotifyLocalService next(Handler<AsyncResult<Void>> handler) {
    checkConnection(handler, () -> player.Next());
    return this;
  }

  @Override
  public SpotifyLocalService togglePlayback(Handler<AsyncResult<Void>> handler) {
    checkConnection(handler, () -> player.PlayPause());
    return this;
  }

  @Override
  public SpotifyLocalService getCurrentSong(Handler<AsyncResult<JsonObject>> handler) {
    checkConnection(handler, () -> {
      Map<String, Variant> metadata = player.Get(DBUS_SERVICE_PLAYER, "Metadata");
      metadata.forEach((key, value) -> Logs.info("Metadata -> Key: {}, Value: {}", key, value));
    });
    return this;
  }

  @Override
  public SpotifyLocalService playTrack(String uri, Handler<AsyncResult<Void>> handler) {
    C.check(uri.startsWith("spotify:track:"),
            () -> checkConnection(handler, () -> player.OpenUri(uri)),
            () -> handler.handle(F.fail("Uri does not start with 'spotify:track:'.")));
    return this;
  }

  @Override
  public SpotifyLocalService playAlbum(String uri, Handler<AsyncResult<Void>> handler) {
    C.check(uri.startsWith("spotify:album:"),
            () -> checkConnection(handler, () -> player.OpenUri(uri)),
            () -> handler.handle(F.fail("Uri does not start with 'spotify:album:'.")));
    return this;
  }

  @Override
  public SpotifyLocalService seek(long position, Handler<AsyncResult<Void>> handler) {
    checkConnection(handler, () -> player.Seek(position));
    return this;
  }

  @Override
  public void close() {
    disconnect();
    super.close();
  }

  @DBusInterfaceName(DBUS_SERVICE_PLAYER)
  private interface Player extends DBusInterface, DBus.Properties {
    void Play();

    void Stop();

    void PlayPause();

    void Previous();

    void Next();

    void OpenUri(String Uri);

    void Seek(long Offset);
  }

  private String getDBusAddressFromFile() {
    List<String> conf = Config.readFile(DBUS_ADDRESS_LOCATION);
    return conf == null || conf.isEmpty() || conf.size() > 1 ? null : conf.get(0);
  }

  private boolean connect() {
    if (conn != null) {
      return true;
    }
    try {
      conn = DBusConnection.getConnection(dbusAddress);
      return true;
    } catch (DBusException e) {
      Logs.error("Failed to connect to DBus using address {}.", dbusAddress, e);
    }
    return false;
  }

  private void disconnect() {
    player = null;
    if (conn != null) {
      conn.disconnect();
      conn = null;
    }
  }

  private boolean getPlayer() {
    if (player != null) {
      return true;
    }
    try {
      player = conn.getRemoteObject(DBUS_SERVICE_SPOTIFY, DBUS_MPRIS_PLAYER, Player.class);
      return true;
    } catch (DBusException e) {
      Logs.error("Failed to get Spotify DBus object.", e);
    }
    return false;
  }
}
