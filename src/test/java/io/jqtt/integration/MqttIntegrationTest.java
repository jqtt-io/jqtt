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

package io.jqtt.integration;

import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import io.jqtt.broker.Broker;
import io.jqtt.broker.BrokerImpl;
import io.jqtt.broker.entrypoint.EntrypointComposition;
import io.jqtt.broker.entrypoint.TcpSocketEntrypointImpl;
import io.jqtt.configuration.Configuration;
import io.jqtt.configuration.ConfigurationImplFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MqttIntegrationTest {

  private static Thread thread;

  @BeforeClass
  public static void setUp() throws Exception {
    final Configuration configuration = ConfigurationImplFactory.create();
    final EntrypointComposition entrypointComposition =
        new EntrypointComposition(new TcpSocketEntrypointImpl());
    final Broker broker = new BrokerImpl(configuration, entrypointComposition);

    thread =
        new Thread(
            () -> {
              try {
                broker.start();
              } catch (Exception ex) {

              }
            });

    thread.start();

    Thread.sleep(5000);
  }

  @Test
  public void tryServer() throws Exception {
    final Mqtt3BlockingClient client =
        Mqtt3Client.builder()
            .identifier("123123123")
            .serverHost("localhost")
            .serverPort(9000)
            .buildBlocking();

    client.connect(new Mqtt3ConnectViewBuilder.Default().cleanSession(false).build());
  }

  @AfterClass
  public static void tearDown() throws Exception {
    Thread.sleep(1000);

    thread.interrupt();
    while (thread.isAlive()) {
      Thread.sleep(250);
    }
  }
}
