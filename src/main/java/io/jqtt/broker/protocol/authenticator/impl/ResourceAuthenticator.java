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

package io.jqtt.broker.protocol.authenticator.impl;

import static java.util.Optional.ofNullable;

import io.jqtt.broker.protocol.authenticator.Authenticator;
import io.jqtt.broker.protocol.model.ClientId;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
public class ResourceAuthenticator implements Authenticator {

  private Map<String, String> passwords;

  public ResourceAuthenticator(final @NonNull String path) {
    final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    final Optional<InputStream> resourceAsStream =
        ofNullable(classloader.getResourceAsStream(path));

    if (resourceAsStream.isPresent()) {
      final Stream<String> lines =
          new BufferedReader(new InputStreamReader(resourceAsStream.get())).lines();
      this.passwords =
          lines
              .map(e -> e.split(":"))
              .filter(e -> e.length == 2)
              .collect(Collectors.toMap(e -> e[0], e -> e[1]));
    } else {
      log.error("Failed to load users from resource {}", path);
    }
  }

  @Override
  public Boolean authenticate(
      final @NonNull ClientId clientId,
      final @NonNull String username,
      final @NonNull byte[] password) {
    Optional<String> foundUsername = ofNullable(this.passwords.get(username));

    return foundUsername.isPresent() && foundUsername.get().equals(DigestUtils.sha256Hex(password));
  }
}
