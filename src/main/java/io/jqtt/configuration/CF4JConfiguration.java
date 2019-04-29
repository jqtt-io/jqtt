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

package io.jqtt.configuration;

import java.util.*;
import java.util.stream.Collectors;

import io.atomix.utils.net.Address;
import org.cfg4j.provider.ConfigurationProvider;

public class CF4JConfiguration implements Configuration {
  private static final String UUID = "@UUID@";

  private final ConfigurationProvider provider;

  private String memberId;

  public CF4JConfiguration(ConfigurationProvider provider) {
    this.provider = provider;
    resolveMemberId();
  }

  @Override
  public boolean isServiceTcpEnabled() {
    return getProperty(SERVICE_TCP_ENABLED, Boolean.class);
  }

  @Override
  public Address serviceTcpAddress() {
    return Address.from(
            getProperty(SERVICE_TCP_HOST, String.class),
            getProperty(SERVICE_TCP_PORT, Integer.class)
    );
  }

  @Override
  public String clusterMemberId() {
    return memberId;
  }

  @Override
  public String clusterClass() {
    return getProperty(CLUSTER_CLASS, String.class);
  }

  @Override
  public String clusterAtomixMembership() {
    return getProperty(CLUSTER_ATOMIX_MEMBERSHIP, String.class);
  }

  @Override
  public String clusterAtomixDiscovery() {
    return getProperty(CLUSTER_ATOMIX_DISCOVERY, String.class);
  }

  @Override
  public String clusterId() {
    return getProperty(CLUSTER_ID, String.class);
  }

  @Override
  public String clusterMemberHost() {
    return getProperty(CLUSTER_MEMBER_HOST, String.class);
  }

  @Override
  public int clusterMemberPort() {
    return getProperty(CLUSTER_MEMBER_PORT, Integer.class);
  }

  @Override
  public Set<String> clusterAtomixDiscoveryBootstrapNodes() {
    return Arrays.stream(
            getProperty(CLUSTER_ATOMIX_DISCOVERY_BOOTSTRAP_NODES, String.class).split(","))
        .map(String::trim)
        .collect(Collectors.toSet());
  }

  @Override
  public String getAuthenticatorClass() {
    return getProperty(Configuration.AUTHENTICATOR_CLASS, String.class);
  }

  @Override
  public Boolean getAllowAnonymous() {
    return getProperty(Configuration.ALLOW_ANONYMOUS, Boolean.class);
  }

  @Override
  public String getAuthenticatorFilePath() {
    return getProperty(Configuration.AUTHENTICATOR_FILE_PATH, String.class);
  }

  @Override
  public <T> T getProperty(String propertyName, Class<T> type) {
    return provider.getProperty(propertyName, type);
  }

  @Override
  public <T> T getProperty(String propertyName, Class<T> type, T defaultValue) {
    try {
      return provider.getProperty(propertyName, type);
    } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException exception) {
      return defaultValue;
    }
  }

  private void resolveMemberId() {
    this.memberId = getProperty(Configuration.CLUSTER_MEMBER_ID, String.class);
    if (this.memberId.equalsIgnoreCase(UUID)) {
      this.memberId = java.util.UUID.randomUUID().toString();
    }
  }
}
