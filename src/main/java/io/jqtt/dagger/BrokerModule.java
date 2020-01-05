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

package io.jqtt.dagger;

import dagger.Module;
import dagger.Provides;
import io.atomix.utils.Managed;
import io.atomix.utils.net.Address;
import io.jqtt.broker.Broker;
import io.jqtt.broker.service.ChannelInitializerFactory;
import io.jqtt.broker.service.DisabledService;
import io.jqtt.broker.service.NettyService;
import io.jqtt.configuration.Configuration;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class BrokerModule {
  @Provides
  @Singleton
  public Broker broker(@Named(Names.JQTT_SERVICE_TCP) Managed tcpService) {
    return Broker.builder().tcpSocketService(tcpService).build();
  }

  @Provides
  @Singleton
  @Named(Names.JQTT_SERVICE_TCP_CHANNEL_INITIALIZER)
  public ChannelInitializer tcpChannelInitializer(
      @Named(Configuration.SERVICE_TCP_NETTY_WRITER_IDLE_TIME) Long writerIdleTimeSeconds,
      @Named(Configuration.SERVICE_TCP_NETTY_READER_IDLE_TIME) Long readerIdleTimeSeconds,
      @Named(Configuration.SERVICE_TCP_NETTY_ALL_IDLE_TIME) Long allIdleTimeSeconds) {
    return new ChannelInitializerFactory()
        .create(
            new IdleStateHandler(
                writerIdleTimeSeconds,
                readerIdleTimeSeconds,
                allIdleTimeSeconds,
                TimeUnit.MILLISECONDS));
  }

  @Provides
  @Singleton
  @Named(Names.JQTT_SERVICE_TCP)
  public Managed tcpService(
      @Named(Configuration.SERVICE_TCP_ENABLED) boolean serviceTcpEnabled,
      @Named(Names.JQTT_SERVICE_TCP_CHANNEL_INITIALIZER) ChannelInitializer channelInitializer,
      @Named(Configuration.SERVICE_TCP_ADDRESS) Address address) {
    if (!serviceTcpEnabled) {
      return new DisabledService(Names.JQTT_SERVICE_TCP, address);
    }
    return new NettyService(channelInitializer, address);
  }
}
