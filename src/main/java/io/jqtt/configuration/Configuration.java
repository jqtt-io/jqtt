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

import io.atomix.utils.net.Address;

import java.util.Set;

public interface Configuration {
  public static final String SERVICE_TCP_ENABLED = "jqtt.service.tcp.enabled";
  public static final String SERVICE_TCP_HOST = "jqtt.service.tcp.host";
  public static final String SERVICE_TCP_PORT = "jqtt.service.tcp.port";
  public static final String CLUSTER_MEMBER_ID = "jqtt.cluster.member.id";
  public static final String CLUSTER_CLASS = "jqtt.cluster.class";
  public static final String CLUSTER_ID = "jqtt.cluster.id";
  public static final String CLUSTER_MEMBER_HOST = "jqtt.cluster.member.host";
  public static final String CLUSTER_MEMBER_PORT = "jqtt.cluster.member.port";
  public static final String CLUSTER_ATOMIX_MEMBERSHIP = "jqtt.cluster.atomix.membership";
  public static final String CLUSTER_ATOMIX_DISCOVERY = "jqtt.cluster.atomix.discovery";
  public static final String CLUSTER_ATOMIX_DISCOVERY_BOOTSTRAP_NODES =
      "jqtt.cluster.atomix.discovery.bootstrap.nodes";
  public static final String AUTHENTICATOR_CLASS = "jqtt.authenticator.class";
  public static final String ALLOW_ANONYMOUS = "jqtt.authenticator.allow_anonymous";
  public static final String AUTHENTICATOR_FILE_PATH = "jqtt.authenticator.file.path";

  boolean isServiceTcpEnabled();

  Address serviceTcpAddress();

  String clusterMemberId();

  String clusterClass();

  String clusterId();

  String clusterMemberHost();

  int clusterMemberPort();

  String clusterAtomixMembership();

  String clusterAtomixDiscovery();

  Set<String> clusterAtomixDiscoveryBootstrapNodes();

  String getAuthenticatorClass();

  Boolean getAllowAnonymous();

  String getAuthenticatorFilePath();

  <T> T getProperty(String propertyName, Class<T> type);

  <T> T getProperty(String propertyName, Class<T> type, T defaultValue);
}
