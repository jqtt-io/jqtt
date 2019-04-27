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

package io.jqtt.cluster.factory.atomix;

import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.cluster.discovery.MulticastDiscoveryProvider;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import io.jqtt.configuration.Configuration;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
  public static final Pattern nodePattern =
      Pattern.compile("(?<memberId>.+)@(?<address>.+):(?<port>\\d+)");

  public static NodeDiscoveryProvider multicastDiscoveryProvider(Configuration configuration) {
    return MulticastDiscoveryProvider.builder().build();
  }

  public static NodeDiscoveryProvider bootstrapDiscoveryProvider(Configuration configuration) {
    final Collection<Node> seeds =
        configuration.clusterAtomixDiscoveryBootstrapNodes().stream()
            .map(nodePattern::matcher)
            .filter(matcher -> matcher.find())
            .map(
                matcher ->
                    Node.builder()
                        .withId(matcher.group("memberId"))
                        .withPort(Integer.parseInt(matcher.group("port")))
                        .withHost(matcher.group("address"))
                        .build())
            .collect(Collectors.toSet());

    return BootstrapDiscoveryProvider.builder().withNodes(seeds).build();
  }
}
