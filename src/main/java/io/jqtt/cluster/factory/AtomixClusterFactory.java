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

package io.jqtt.cluster.factory;

import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import io.atomix.cluster.protocol.GroupMembershipProtocol;
import io.atomix.cluster.protocol.SwimMembershipProtocol;
import io.atomix.core.Atomix;
import io.atomix.core.AtomixBuilder;
import io.atomix.primitive.partition.ManagedPartitionGroup;
import io.atomix.primitive.partition.MemberGroupStrategy;
import io.atomix.primitive.protocol.PrimitiveProtocol;
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup;
import io.atomix.protocols.gossip.AntiEntropyProtocol;
import io.jqtt.cluster.AtomixClusterService;
import io.jqtt.cluster.ClusterService;
import io.jqtt.cluster.factory.atomix.Utils;
import io.jqtt.configuration.Configuration;
import io.jqtt.exception.ClusterException;

public class AtomixClusterFactory implements ClusterFactory {
  private static final String DISCOVERY_MULTICAST = "multicast";
  private static final String DISCOVERY_BOOTSTRAP = "bootstrap";
  private static final String MEMBERSHIP_SWIM = "swim";
  private static final String PARTITION_NAME = "jqtt";

  private final Configuration configuration;

  public AtomixClusterFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public ClusterService create() throws ClusterException {
    ManagedPartitionGroup managedPartitionGroup =
        PrimaryBackupPartitionGroup.builder(PARTITION_NAME)
            .withNumPartitions(1)
            .withMemberGroupStrategy(MemberGroupStrategy.NODE_AWARE)
            .build();

    AtomixBuilder builder =
        Atomix.builder()
            .withShutdownHookEnabled()
            .withMulticastEnabled(
                configuration.clusterAtomixDiscovery().equalsIgnoreCase(DISCOVERY_MULTICAST))
            .withClusterId(configuration.clusterId())
            .withHost(configuration.clusterMemberHost())
            .withPort(configuration.clusterMemberPort())
            .withMemberId(configuration.clusterMemberId())
            .withMembershipProvider(nodeDiscoveryProvider(configuration))
            .withMembershipProtocol(groupMembershipProtocol(configuration))
            .withManagementGroup(managedPartitionGroup)
            .withPartitionGroups(managedPartitionGroup);

    PrimitiveProtocol primitiveProtocol = AntiEntropyProtocol.builder().build();

    return new AtomixClusterService(builder.build(), primitiveProtocol);
  }

  private GroupMembershipProtocol groupMembershipProtocol(Configuration configuration)
      throws ClusterException {
    final String membership = configuration.clusterAtomixMembership();
    GroupMembershipProtocol groupMembershipProtocol;

    if (membership.equalsIgnoreCase(MEMBERSHIP_SWIM)) {
      groupMembershipProtocol = SwimMembershipProtocol.builder().build();
    } else {
      throw new ClusterException(String.format("Unknown membership protocol %s.", membership));
    }
    return groupMembershipProtocol;
  }

  private NodeDiscoveryProvider nodeDiscoveryProvider(Configuration configuration)
      throws ClusterException {
    final String discovery = configuration.clusterAtomixDiscovery();
    NodeDiscoveryProvider nodeDiscoveryProvider;

    if (discovery.equalsIgnoreCase(DISCOVERY_MULTICAST)) {
      nodeDiscoveryProvider = Utils.multicastDiscoveryProvider(configuration);
    } else if (discovery.equalsIgnoreCase(DISCOVERY_BOOTSTRAP)) {
      nodeDiscoveryProvider = Utils.bootstrapDiscoveryProvider(configuration);
    } else {
      throw new ClusterException(String.format("Unknown Utils provider %s.", discovery));
    }
    return nodeDiscoveryProvider;
  }
}
