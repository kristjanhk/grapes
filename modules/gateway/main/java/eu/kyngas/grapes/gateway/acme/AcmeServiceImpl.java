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

package eu.kyngas.grapes.gateway.acme;

import eu.kyngas.grapes.common.logging.Loggable;
import eu.kyngas.grapes.common.util.Ctx;
import eu.kyngas.grapes.common.util.F;
import eu.kyngas.grapes.common.util.Logs;
import eu.kyngas.grapes.common.util.Unsafe;
import eu.kyngas.grapes.gateway.http.HttpService;
import eu.kyngas.grapes.gateway.zone.ZoneService;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeRetryAfterException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class AcmeServiceImpl implements AcmeService {
  private final static String USER_KEY_PAIR = "user_keypair.pem";
  private final JsonObject config;
  private final ZoneService zoneService;

  public AcmeServiceImpl(JsonObject config) {
    Objects.requireNonNull(config.getString("uri"), "Config: acme.json 'uri' param is undefined.");
    Objects.requireNonNull(config.getString("domains"), "Config: acme.json 'domains' param is undefined.");
    if (config.getJsonObject("domains").isEmpty()) {
      throw new NullPointerException("Config: acme.json must contain atleast a single domain.");
    }
    this.config = config;
    this.zoneService = ZoneService.create();
  }

  @Loggable
  @Override
  public Future<Void> renewCertificate(String domainKey) {
    if (domainKey == null) {
      return F.fail("DomainKey cannot be null.");
    }
    Session session = new Session(config.getString("uri"));
    Collection<String> domains = Unsafe.cast(config.getJsonObject("domains")
                                                 .getJsonArray(domainKey, new JsonArray())
                                                 .getList());
    if (domains.isEmpty()) {
      Logs.warn("No domains found for domainKey '{}'.", domainKey);
      return F.success();
    }

    Future<Account> account = Ctx.blocking(() -> getOrCreateAccount(session, getOrCreateKeyPair(USER_KEY_PAIR)));
    Future<KeyPair> keyPairFuture =
        Ctx.blocking(() -> getOrCreateKeyPair(normalizeDomain(domainKey) + ".pem"));

    return F.all(account, keyPairFuture)
        .compose(all -> createOrder(account.result(), domainKey, keyPairFuture.result(), domains));
  }

  @Loggable
  private Future<Void> createOrder(Account account, String domainKey, KeyPair keyPair, Collection<String> domains) {
    return F.future(() -> createOrderAuthorizations(account, domains))
        .compose(iterator -> doAuthorizations(iterator, domainKey, keyPair, domains));
  }

  @Loggable
  private Future<Void> doAuthorizations(Iterator<Authorization> iterator,
                                        String domainKey,
                                        KeyPair keyPair,
                                        Collection<String> domains) {
    if (!iterator.hasNext()) {
      return signCsr(domainKey, keyPair, domains);
    }
    Authorization auth = iterator.next();
    return doHttpChallenge(auth)
        .recover(err -> doDnsChallenge(auth))
        .recover(err -> doTlsAlpnChallenge(auth))
        .compose(v -> doAuthorizations(iterator, domainKey, keyPair, domains));
  }

  private Future<Void> doHttpChallenge(Authorization auth) {
    if (auth.getStatus() == Status.VALID) {
      return F.success();
    }
    if (!config.getBoolean("use-http-challenge", false)) {
      return F.fail("HTTP challenge is disabled.");
    }
    if (!Ctx.isProductionMode()) {
      return F.fail("HTTP challenge can only be completed on production host.");
    }
    Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
    if (challenge == null) {
      return F.fail("Authorization did not contain HTTP challenge for domain %s.", auth.getDomain());
    }
    String url = "/well-known/acme-challenge/" + challenge.getToken();
    String content = challenge.getAuthorization();
    HttpService httpService = HttpService.createProxy().addRoute(url, content);
    return triggerChallenge(challenge)
        .compose(v -> F.success(httpService.removeRoute(url)))
        .mapEmpty(); // TODO: 6.05.2018 mapEmpty is ok?
  }

  // TODO: 7.05.2018 dns propagation can take a while -> expect atleast 5 minutes, need to timeout max 60min
  private Future<Void> doDnsChallenge(Authorization auth) {
    if (auth.getStatus() == Status.VALID) {
      return F.success();
    }
    if (!config.getBoolean("use-dns-challenge", false)) {
      return F.fail("DNS challenge is disabled.");
    }
    Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
    if (challenge == null) {
      return F.fail("Authorization did not contain DNS challenge for domain %s.", auth.getDomain());
    }
    String digest = challenge.getDigest();
    return zoneService.createDnsTxtRecord(auth.getDomain(), digest)
        .compose(v -> triggerChallenge(challenge))
        .compose(v -> zoneService.deleteDnsTxtRecord(auth.getDomain()))
        .recover(v -> zoneService.deleteDnsTxtRecord(auth.getDomain()));
  }

  private Future<Void> doTlsAlpnChallenge(Authorization auth) {
    if (auth.getStatus() == Status.VALID) {
      return F.success();
    }
    if (!config.getBoolean("use-tls-alpn-challenge", false)) {
      return F.fail("TLS-ALPN challenge is disabled.");
    }
    if (!Ctx.isProductionMode()) {
      return F.fail("TLS-ALPN challenge can only be completed on production host.");
    }
    TlsAlpn01Challenge challenge = auth.findChallenge(TlsAlpn01Challenge.TYPE);
    if (challenge == null) {
      return F.fail("Authorization did not contain TLS ALPN challenge for domain %s.", auth.getDomain());
    }
    byte[] acmeValidation = challenge.getAcmeValidationV1();
    // TODO: 22.04.2018 create self-signed certificate and respond to acme 443 requests with cert
    // TODO: 22.04.2018 https://shredzone.org/maven/acme4j/challenge/tls-alpn-01.html
    return triggerChallenge(challenge)
        .compose(v -> F.success()); // TODO: 6.05.2018 rm route
  }

  private Future<Void> signCsr(String domainKey, KeyPair keyPair, Collection<String> domains) {
    CSRBuilder csrBuilder = new CSRBuilder();
    csrBuilder.addDomains(domains);
    return Ctx.blocking(() -> {
      csrBuilder.sign(keyPair);
      try (FileWriter fw = new FileWriter(normalizeDomain(domainKey) + ".csr")) {
        csrBuilder.write(fw);
      }
    });
  }

  private Future<Void> triggerChallenge(Challenge challenge) {
    return F.future(future -> Ctx.blocking(challenge::trigger)
        .compose(v -> F.<Void>future(fut -> waitChallenge(challenge, fut, 10))));
  }

  private void waitChallenge(Challenge challenge, Future future, int countdown) {
    if (countdown <= 0) {
      future.fail("Challenge wait timeout, error: " + challenge.getError());
      return;
    }
    if (challenge.getStatus() == Status.VALID) {
      future.complete();
      return;
    }
    long sleepMs = TimeUnit.SECONDS.toMillis(2);
    try {
      challenge.update();
    } catch (AcmeRetryAfterException e) {
      sleepMs = e.getRetryAfter().toEpochMilli() - System.currentTimeMillis() + 200L;
    } catch (AcmeException e) {
      future.fail(new Throwable("Challenge exception.", e));
      return;
    }
    Ctx.sleep(sleepMs, t -> waitChallenge(challenge, future, countdown - 1));
  }

  private KeyPair getOrCreateKeyPair(String fileName) throws IOException {
    InputStream keyPairIn = getClass().getResourceAsStream(fileName);
    if (keyPairIn != null) {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(keyPairIn))) {
        Logs.info("Reading keypair {} from jar.", fileName);
        return KeyPairUtils.readKeyPair(br);
      }
    }
    File keypairFile = new File(fileName);
    if (keypairFile.exists()) {
      try (FileReader fr = new FileReader(keypairFile)) {
        Logs.info("Reading keypair {} from local directory.", fileName);
        return KeyPairUtils.readKeyPair(fr);
      }
    }
    KeyPair keyPair = KeyPairUtils.createKeyPair(4096);
    try (FileWriter fw = new FileWriter(keypairFile)) {
      Logs.info("Creating new keypair {}.", fileName);
      KeyPairUtils.writeKeyPair(keyPair, fw);
    }
    return keyPair;
  }

  private Account getOrCreateAccount(Session session, KeyPair keyPair) throws AcmeException {
    return new AccountBuilder()
        .agreeToTermsOfService()
        .useKeyPair(keyPair)
        .create(session);
  }

  private Iterator<Authorization> createOrderAuthorizations(Account account, Collection<String> domains)
      throws AcmeException {
    return account.newOrder()
        .domains(domains)
        .create()
        .getAuthorizations()
        .iterator();
  }

  private String normalizeDomain(String name) {
    return name == null ? null : name.replaceAll("\\.", "_");
  }
}
