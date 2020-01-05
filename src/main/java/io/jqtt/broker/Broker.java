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

package io.jqtt.broker;

import io.atomix.utils.Managed;
import io.atomix.utils.concurrent.SingleThreadContext;
import io.atomix.utils.concurrent.ThreadContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class Broker implements Managed<Void> {
  private final Managed tcpSocketService;

  private final ThreadContext threadContext = new SingleThreadContext("jqtt-broker-%d");
  private final AtomicBoolean started = new AtomicBoolean();

  @Override
  public synchronized CompletableFuture<Void> start() {
    return startServices().thenComposeAsync(v -> completeStartup(), threadContext);
  }

  @Override
  public boolean isRunning() {
    return started.get();
  }

  @Override
  public synchronized CompletableFuture<Void> stop() {
    return stopServices().thenComposeAsync(v -> completeShutdown(), threadContext);
  }

  private CompletableFuture<Void> completeStartup() {
    started.set(true);
    return CompletableFuture.completedFuture(null);
  }

  private CompletableFuture<Void> completeShutdown() {
    threadContext.close();
    started.set(false);
    return CompletableFuture.completedFuture(null);
  }

  private CompletableFuture<Void> startServices() {
    return tcpSocketService
        .start()
        .thenRun(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> this.stop().join())))
        .thenApply(v -> null);
  }

  private CompletableFuture<Void> stopServices() {
    return tcpSocketService.stop().exceptionally(e -> null);
  }
}
