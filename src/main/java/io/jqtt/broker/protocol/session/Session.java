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

import io.jqtt.broker.protocol.connection.MqttConnection;
import io.jqtt.broker.protocol.model.ClientId;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;

public final class Session {

  private final ClientId clientId;
  private final Boolean isClean;
  private final MqttConnection mqttConnection;
  private final AtomicReference<Status> status = new AtomicReference<>(Status.DISCONNECTED);

  private Session(
      final @NonNull ClientId clientId,
      final @NonNull Boolean isClean,
      final @NonNull MqttConnection mqttConnection) {
    this.clientId = clientId;
    this.isClean = isClean;
    this.mqttConnection = mqttConnection;

    this.connect();
  }

  public static Session create(
      final @NonNull MqttConnection mqttConnection,
      final @NonNull ClientId clientId,
      final @NonNull MqttConnectMessage mqttConnectMessage) {

    return new Session(
        clientId, mqttConnectMessage.variableHeader().isCleanSession(), mqttConnection);
  }

  private void connect() {
    status.compareAndSet(Status.DISCONNECTED, Status.CONNECTED);
  }

  public ClientId clientId() {
    return clientId;
  }

  public Boolean isClean() {
    return isClean;
  }

  public boolean disconnected() {
    return status.get() == Status.DISCONNECTED;
  }

  public boolean connected() {
    return status.get() == Status.CONNECTED;
  }
}
