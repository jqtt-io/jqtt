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

package io.jqtt.cluster;

import io.atomix.core.Atomix;
import io.atomix.primitive.protocol.PrimitiveProtocol;
import io.jqtt.cluster.event.service.AtomixEventService;
import io.jqtt.cluster.event.service.EventService;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AtomixClusterService implements ClusterService {
  private final Atomix atomix;
  private final PrimitiveProtocol primitiveProtocol;
  private EventService eventService;

  public AtomixClusterService(Atomix atomix, PrimitiveProtocol primitiveProtocol) {
    this.atomix = atomix;
    this.primitiveProtocol = primitiveProtocol;
  }

  @Override
  public CompletableFuture<ClusterService> start() {
    return atomix.start().thenApply(v -> this);
  }

  @Override
  public boolean isRunning() {
    return atomix.isRunning();
  }

  @Override
  public CompletableFuture<Void> stop() {
    return atomix.stop();
  }

  @Override
  public EventService eventService() {
    if (Objects.isNull(eventService)) {
      eventService = new AtomixEventService(atomix);
    }
    return eventService;
  }
}
