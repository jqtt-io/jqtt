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

package io.jqtt.broker.protocol.session;

import static io.jqtt.broker.protocol.connection.MqttConnectionUtils.ackConnection;

import io.jqtt.broker.protocol.connection.MqttConnection;
import io.jqtt.broker.protocol.model.ClientId;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionExecutor {

  private final SessionManager sessionManager;
  private final MqttConnection mqttConnection;

  public SessionExecutor(SessionManager sessionManager, MqttConnection mqttConnection) {
    this.sessionManager = sessionManager;
    this.mqttConnection = mqttConnection;
  }

  public MqttConnAckMessage execute(
      final @NonNull ClientId clientId, final @NonNull MqttConnectMessage mqttConnectMessage) {
    final Session session = Session.create(mqttConnection, clientId, mqttConnectMessage);

    if (sessionManager.exists(session.clientId())) {
      bindToExistingSession(session.clientId());
    } else {
      storeOrBindToExistingSession(session);
    }

    return ackConnection(false);
  }

  private void storeOrBindToExistingSession(Session session) {
    if (sessionManager.store(session)) {
      log.info("Session stored {}", session.clientId());
    } else {
      bindToExistingSession(session.clientId());
    }
  }

  private void bindToExistingSession(ClientId clientId) {
    Optional<Session> previousSession = sessionManager.fetch(clientId);
  }
}
