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

package io.jqtt.broker.entrypoint;

import io.jqtt.broker.handler.MqttServerHandler;
import io.jqtt.broker.protocol.authenticator.AuthenticatorFactory;
import io.jqtt.broker.protocol.session.SessionManager;
import io.jqtt.configuration.Configuration;
import io.jqtt.exception.JqttExcepion;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpSocketEntrypointImpl implements Entrypoint {

  private final Configuration configuration;
  private final AuthenticatorFactory authenticatorFactory;
  private final SessionManager sessionManager;

  public TcpSocketEntrypointImpl(
      final @NonNull Configuration configuration,
      final @NonNull AuthenticatorFactory authenticatorFactory,
      final @NonNull SessionManager sessionManager) {

    this.configuration = configuration;
    this.authenticatorFactory = authenticatorFactory;
    this.sessionManager = sessionManager;
  }

  @Override
  public void start() throws JqttExcepion {
    final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    final EventLoopGroup handlerGroup = new NioEventLoopGroup();
    final ChannelGroup channelGroup = new DefaultChannelGroup("mqtt_channel_group", null);

    try {
      final ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(
              new ChannelInitializer() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                  final ChannelPipeline pipeline = ch.pipeline();

                  pipeline.addFirst("idleHandler", new IdleStateHandler(0, 0, 2000));
                  pipeline.addLast("encoder", new MqttDecoder());
                  pipeline.addLast("decoder", MqttEncoder.INSTANCE);
                  pipeline.addLast(
                      new MqttServerHandler(
                          configuration, authenticatorFactory.create(), sessionManager));
                }
              });

      final ChannelFuture startServerFuture = serverBootstrap.bind(9000).sync();
      startServerFuture.channel().closeFuture().sync();
    } catch (Exception e) {
      throw new JqttExcepion("Exception during bootstrapping error server", e);
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
