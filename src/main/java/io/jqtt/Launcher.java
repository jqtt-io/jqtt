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

package io.jqtt;

import io.jqtt.broker.Broker;
import io.jqtt.broker.protocol.authenticator.Authenticator;
import io.jqtt.broker.protocol.authenticator.AuthenticatorFactory;
import io.jqtt.broker.protocol.session.SessionManager;
import io.jqtt.broker.protocol.session.impl.InMemorySessionManager;
import io.jqtt.broker.service.NettyService;
import io.jqtt.broker.service.TcpSocketServiceFactory;
import io.jqtt.cluster.ClusterService;
import io.jqtt.cluster.factory.ClusterFactory;
import io.jqtt.configuration.Configuration;
import io.jqtt.configuration.ConfigurationFactory;
import io.jqtt.exception.JqttExcepion;
import io.jqtt.utils.ClassLoader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Launcher {
  public static void main(String[] args) {
    try {
      final Configuration configuration = ConfigurationFactory.create();

      final Broker broker =
          Broker.builder()
              .clusterService(clusterService(configuration))
              .tcpSocketService(tcpSocketService(configuration))
              .build();

      broker.start().join();

    } catch (Exception e) {
      log.error("Exception during bootstrapping broker.", e);
      System.exit(1);
    }
  }

  private static NettyService tcpSocketService(Configuration configuration) {
    return new TcpSocketServiceFactory(
            configuration,
            initializeAuthenticatorFactory(configuration),
            initializeSessionManager())
        .create();
  }

  private static ClusterService clusterService(Configuration configuration) throws JqttExcepion {
    final @NonNull String clusterClass = configuration.clusterClass();

    return ClassLoader.loadClass(
            clusterClass, ClusterFactory.class, Configuration.class, configuration)
        .create();
  }

  private static Authenticator initializeAuthenticatorFactory(Configuration configuration) {
    final @NonNull String authenticatorClass = configuration.getAuthenticatorClass();

    return ClassLoader.loadClass(
            authenticatorClass, AuthenticatorFactory.class, Configuration.class, configuration)
        .create();
  }

  private static SessionManager initializeSessionManager() {
    return new InMemorySessionManager();
  }
}
