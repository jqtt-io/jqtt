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

package io.jqtt.broker.protocol.connection;

import static io.jqtt.broker.protocol.connection.MqttConnectionUtils.abortConnection;
import static io.jqtt.broker.protocol.connection.MqttConnectionUtils.checkProtocolVersion;

import io.jqtt.broker.protocol.authenticator.Authenticator;
import io.jqtt.broker.protocol.authenticator.AuthenticatorExecutor;
import io.jqtt.broker.protocol.exception.BadUsernameOrPasswordException;
import io.jqtt.broker.protocol.exception.IdentifierRejectionException;
import io.jqtt.broker.protocol.exception.UnacceptableProtocolVersionException;
import io.jqtt.broker.protocol.model.ClientId;
import io.jqtt.broker.protocol.session.SessionExecutor;
import io.jqtt.broker.protocol.session.SessionManager;
import io.jqtt.configuration.Configuration;
import io.jqtt.exception.JqttExcepion;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MqttConnection {

  private final AuthenticatorExecutor authenticatorExecutor;
  private final SessionExecutor sessionExecutor;

  public MqttConnection(
      final @NonNull Channel channel,
      final @NonNull Authenticator authenticator,
      final @NonNull SessionManager sessionManager,
      final @NonNull Configuration configuration) {
    this.authenticatorExecutor =
        new AuthenticatorExecutor(authenticator, configuration.getAllowAnonymous());
    this.sessionExecutor = new SessionExecutor(sessionManager, this);
  }

  public MqttMessage connect(
      final @NonNull MqttMessage message, final @NonNull ChannelHandlerContext ctx) {
    final MqttConnectMessage mqttConnectMessage = (MqttConnectMessage) message;

    try {
      return processConnection(mqttConnectMessage, ctx);
    } catch (UnacceptableProtocolVersionException e) {
      return abortConnection(
          MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
    } catch (IdentifierRejectionException ex) {
      return abortConnection(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
    } catch (BadUsernameOrPasswordException e) {
      return abortConnection(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
    } catch (JqttExcepion e) {
      log.error("Exception during CONNECT to broker", e);
    }

    return abortConnection(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
  }

  private MqttMessage processConnection(
      final @NonNull MqttConnectMessage mqttConnectMessage,
      final @NonNull ChannelHandlerContext ctx)
      throws JqttExcepion {
    if (mqttConnectMessage.decoderResult().isFailure()) {
      throw UnacceptableProtocolVersionException.of();
    }

    if (checkProtocolVersion(mqttConnectMessage)) {
      throw UnacceptableProtocolVersionException.of();
    }

    final MqttConnectVariableHeader mqttConnectVariableHeader = mqttConnectMessage.variableHeader();
    final MqttConnectPayload payload = mqttConnectMessage.payload();
    final ClientId clientId = ClientId.create(payload.clientIdentifier());

    if (clientId.isNotPresent() && !mqttConnectVariableHeader.isCleanSession()) {
      throw IdentifierRejectionException.of();
    }

    if (clientId.isNotPresent()) {
      clientId.regenerate();
    }

    if (!authenticatorExecutor.execute(mqttConnectMessage, clientId)) {
      throw BadUsernameOrPasswordException.of();
    }

    return sessionExecutor.execute(clientId, mqttConnectMessage);
  }
}
