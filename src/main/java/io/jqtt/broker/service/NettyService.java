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

import static io.atomix.utils.concurrent.Threads.namedThreads;

import io.atomix.utils.Managed;
import io.atomix.utils.net.Address;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyService implements Managed {
  protected EventLoopGroup serverGroup;
  protected EventLoopGroup clientGroup;
  protected Class<? extends ServerChannel> serverChannelClass;
  protected Class<? extends Channel> clientChannelClass;
  protected Channel serverChannel;

  protected final AtomicBoolean started = new AtomicBoolean();

  protected final ChannelInitializer channelInitializer;
  protected final Address address;

  public NettyService(ChannelInitializer channelInitializer, Address address) {
    this.channelInitializer = channelInitializer;
    this.address = address;
  }

  @Override
  public CompletableFuture<?> start() {
    if (started.get()) {
      log.warn("Already running at local address: {}", address);
      return CompletableFuture.completedFuture(this);
    }

    initEventLoopGroup();

    return bootstrapServer().thenRun(() -> started.set(true)).thenApply(v -> this);
  }

  @Override
  public CompletableFuture<Void> stop() {
    if (started.compareAndSet(true, false)) {
      return CompletableFuture.supplyAsync(
          () -> {
            boolean interrupted = false;
            try {
              try {
                serverChannel.close().sync();
              } catch (InterruptedException e) {
                interrupted = true;
              }
              Future<?> serverShutdownFuture = serverGroup.shutdownGracefully();
              Future<?> clientShutdownFuture = clientGroup.shutdownGracefully();
              try {
                serverShutdownFuture.sync();
              } catch (InterruptedException e) {
                interrupted = true;
              }
              try {
                clientShutdownFuture.sync();
              } catch (InterruptedException e) {
                interrupted = true;
              }
            } finally {
              log.info("Stopped");
              if (interrupted) {
                Thread.currentThread().interrupt();
              }
            }
            return null;
          });
    }
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public boolean isRunning() {
    return started.get();
  }

  protected void initEventLoopGroup() {
    // try Epoll first and if that does work, use nio.
    try {
      clientGroup = new EpollEventLoopGroup(0, namedThreads("jqtt-netty-epoll-client-%d", log));
      serverGroup = new EpollEventLoopGroup(0, namedThreads("jqtt-netty-epoll-server-%d", log));
      serverChannelClass = EpollServerSocketChannel.class;
      clientChannelClass = EpollSocketChannel.class;
      return;
    } catch (Throwable e) {
      log.debug(
          "Failed to initialize native (epoll) transport. " + "Reason: {}. Proceeding with nio.",
          e.getMessage());
    }
    clientGroup = new NioEventLoopGroup(0, namedThreads("jqtt-netty-nio-client-%d", log));
    serverGroup = new NioEventLoopGroup(0, namedThreads("jqtt-netty-nio-server-%d", log));
    serverChannelClass = NioServerSocketChannel.class;
    clientChannelClass = NioSocketChannel.class;
  }

  protected CompletableFuture<Void> bootstrapServer() {
    final ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(serverGroup, clientGroup);
    bootstrap.channel(serverChannelClass);
    bootstrap.childHandler(channelInitializer);

    CompletableFuture<Void> future = new CompletableFuture<>();
    bootstrap
        .bind(address.address(), address.port())
        .addListener(
            (ChannelFutureListener)
                bind -> {
                  if (bind.isSuccess()) {
                    log.info("TCP server listening for connections on {}", address.toString());
                    serverChannel = bind.channel();
                    future.complete(null);
                  } else {
                    log.warn(
                        "Failed to bind TCP server to port {} due to {}",
                        address.toString(),
                        bind.cause());
                    future.completeExceptionally(bind.cause());
                  }
                });

    return future;
  }
}
