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

import static io.atomix.utils.concurrent.Threads.namedThreads;

import io.atomix.utils.Managed;
import io.jqtt.broker.handler.MqttServerHandler;
import io.jqtt.broker.protocol.authenticator.AuthenticatorFactory;
import io.jqtt.broker.protocol.session.SessionManager;
import io.jqtt.configuration.Configuration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpSocketService implements Managed<TcpSocketService> {

  private final Configuration configuration;
  private final AuthenticatorFactory authenticatorFactory;
  private final SessionManager sessionManager;

  private final AtomicBoolean started = new AtomicBoolean();
  private EventLoopGroup group;
  private EventLoopGroup worker;
  private ChannelFuture serverChannel;

  public TcpSocketService(
      Configuration configuration,
      AuthenticatorFactory authenticatorFactory,
      SessionManager sessionManager) {
    this.configuration = configuration;
    this.authenticatorFactory = authenticatorFactory;
    this.sessionManager = sessionManager;
  }

  @Override
  public CompletableFuture<TcpSocketService> start() {
    log.debug("TcpSocketService start");
    group = new NioEventLoopGroup(0, namedThreads("jqtt-tcp-socket-service-%d", log));
    worker = new NioEventLoopGroup();

    return bootstrap().thenRun(() -> started.set(true)).thenApply(v -> this);
  }

  @Override
  public boolean isRunning() {
    return started.get();
  }

  @Override
  public CompletableFuture<Void> stop() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    serverChannel.channel().close().addListener(f -> {
      started.set(false);
      group.shutdownGracefully();
      future.complete(null);
    });

    return future;
  }

  private CompletableFuture<Void> bootstrap() {
    final ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap
        .group(group, worker)
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

    CompletableFuture<Void> future = new CompletableFuture<>();

    serverChannel = serverBootstrap
        .bind(9000)
        .addListener(
            f -> {
              if (!f.isSuccess()) {
                future.completeExceptionally(f.cause());
              } else {
                future.complete(null);
              }
            });

    return future;
  }
}
