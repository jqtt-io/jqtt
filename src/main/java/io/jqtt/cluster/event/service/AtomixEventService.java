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

package io.jqtt.cluster.event.service;

import io.atomix.core.Atomix;
import io.jqtt.cluster.event.MessageWasPublish;
import io.jqtt.cluster.event.TopicWasSubscribed;
import io.jqtt.cluster.event.TopicWasUnsubscribed;
import io.jqtt.cluster.event.listener.OnPublish;
import io.jqtt.cluster.event.listener.OnSubscribe;
import io.jqtt.cluster.event.listener.OnUnsubscribe;

public class AtomixEventService implements EventService {
  private final Atomix atomix;

  public AtomixEventService(Atomix atomix) {
    this.atomix = atomix;
  }

  @Override
  public void publish(MessageWasPublish messageWasPublish) {}

  @Override
  public void subscribe(TopicWasSubscribed messageWasPublish) {}

  @Override
  public void unsubscribe(TopicWasUnsubscribed messageWasPublish) {}

  @Override
  public void listener(OnPublish onPublish) {}

  @Override
  public void listener(OnSubscribe onSubscribe) {}

  @Override
  public void listener(OnUnsubscribe onUnsubscribe) {}
}
