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

package io.jqtt.broker.protocol.session.impl;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.google.common.collect.Maps;
import io.jqtt.broker.protocol.model.ClientId;
import io.jqtt.broker.protocol.session.Session;
import io.jqtt.broker.protocol.session.SessionManager;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import lombok.NonNull;

public class InMemorySessionManager implements SessionManager {

  private final ConcurrentMap<ClientId, Session> sessions;

  public InMemorySessionManager() {
    this.sessions = Maps.newConcurrentMap();
  }

  @Override
  public Boolean exists(final @NonNull ClientId clientId) {
    return sessions.containsKey(clientId);
  }

  @Override
  public Boolean store(@NonNull Session session) {
    final Session previousSession = sessions.putIfAbsent(session.clientId(), session);

    return Objects.isNull(previousSession);
  }

  @Override
  public Optional<Session> fetch(@NonNull ClientId clientId) {
    try {
      return ofNullable(sessions.get(clientId));
    } catch (ClassCastException | NullPointerException ex) {
      return empty();
    }
  }
}
