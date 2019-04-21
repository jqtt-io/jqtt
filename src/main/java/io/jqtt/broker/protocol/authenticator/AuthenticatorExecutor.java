/*
 * MIT License
 *
 * Copyright (c) 2019 jqtt.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.jqtt.broker.protocol.authenticator;

import io.jqtt.broker.protocol.model.ClientId;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AuthenticatorExecutor {
  private final Authenticator authenticator;
  private final Boolean allowAnonymous;

  public AuthenticatorExecutor(
      final @NonNull Authenticator authenticator, final @NonNull Boolean allowAnonymous) {
    this.authenticator = authenticator;
    this.allowAnonymous = allowAnonymous;
  }

  public final Boolean execute(
      final @NonNull MqttConnectMessage msg, final @NonNull ClientId clientId) {
    if (msg.variableHeader().hasUserName()) {
      final String username = msg.payload().userName();

      if (msg.variableHeader().hasPassword()) {
        final byte[] password = msg.payload().passwordInBytes();

        if (authenticator.authenticate(clientId, username, password)) {
          log.debug("Client={}, Username={} was logged in", clientId, username);

          return true;
        }

        log.error("Client={}, Username={} was rejected", clientId, username);

        return false;
      }

      if (allowAnonymous) {
        log.debug("Client={}, Username={} was logged in for anonymous mode", clientId, username);

        return true;
      }

      log.error("Password was not supplied by Client={}, Username={}", clientId, username);

      return false;
    }

    if (allowAnonymous) {
      log.debug("Client={} was logged in for anonymous mode", clientId);

      return true;
    }

    log.error(
        "Credentials were not supplied by client {} and anonymous mode is disabled", clientId);

    return false;
  }
}
