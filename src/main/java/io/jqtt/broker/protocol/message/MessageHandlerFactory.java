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

package io.jqtt.broker.protocol.message;

import io.jqtt.broker.protocol.authenticator.AuthenticatorFactory;
import io.jqtt.broker.protocol.message.impl.ConnectMessageHandler;
import io.jqtt.broker.protocol.message.impl.NoopMessageHandler;
import io.jqtt.configuration.Configuration;
import lombok.NonNull;

public class MessageHandlerFactory {

  private final AuthenticatorFactory authenticatorFactory;
  private final Configuration configuration;

  public MessageHandlerFactory(
      final @NonNull AuthenticatorFactory authenticatorFactory,
      final @NonNull Configuration configuration) {
    this.authenticatorFactory = authenticatorFactory;
    this.configuration = configuration;
  }

  public MessageHandler fromMessage(String message) {
    switch (message) {
      case "CONNECT":
        return new ConnectMessageHandler(authenticatorFactory.create(), configuration);
      default:
        return new NoopMessageHandler();
    }
  }
}
