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

package io.jqtt.broker.service;

import io.atomix.utils.net.Address;
import io.jqtt.broker.handler.MqttServerHandler;
import io.jqtt.broker.protocol.authenticator.Authenticator;
import io.jqtt.broker.protocol.session.SessionManager;
import io.jqtt.configuration.Configuration;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TcpSocketServiceFactory {
  private final Configuration configuration;
  private final Authenticator authenticator;
  private final SessionManager sessionManager;

  public NettyService create() {
    return new NettyService(channelInitializer(), Address.from("127.0.0.1:9000"));
  }

  private ChannelInitializer channelInitializer() {
    return new ChannelInitializer() {
      @Override
      protected void initChannel(Channel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();

        pipeline.addFirst("idleHandler", new IdleStateHandler(0, 0, 2000));
        pipeline.addLast("encoder", new MqttDecoder());
        pipeline.addLast("decoder", MqttEncoder.INSTANCE);
        pipeline.addLast(new MqttServerHandler(configuration, authenticator, sessionManager));
      }
    };
  }
}
