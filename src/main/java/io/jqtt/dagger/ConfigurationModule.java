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
import io.atomix.utils.net.Address;
import io.jqtt.configuration.Configuration;
import io.jqtt.configuration.ConfigurationFactory;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class ConfigurationModule {
  @Provides
  @Singleton
  Configuration configuration() {
    return ConfigurationFactory.create();
  }

  @Provides
  @Singleton
  @Named(Configuration.SERVICE_TCP_ENABLED)
  boolean isTcpServiceEnabled(Configuration configuration) {
    return configuration.getProperty(Configuration.SERVICE_TCP_ENABLED, Boolean.class);
  }

  @Provides
  @Singleton
  @Named(Configuration.SERVICE_TCP_ADDRESS)
  Address serviceTcpAddress(Configuration configuration) {
    return Address.from(
        configuration.getProperty(Configuration.SERVICE_TCP_HOST, String.class),
        configuration.getProperty(Configuration.SERVICE_TCP_PORT, Integer.class));
  }

  @Provides
  @Singleton
  @Named(Configuration.SERVICE_TCP_NETTY_ALL_IDLE_TIME)
  Long serviceTcpNettyAllIdleTimeSeconds(Configuration configuration) {
    return configuration.getProperty(Configuration.SERVICE_TCP_NETTY_ALL_IDLE_TIME, Long.class);
  }

  @Provides
  @Singleton
  @Named(Configuration.SERVICE_TCP_NETTY_READER_IDLE_TIME)
  Long serviceTcpNettyReaderIdleTimeSeconds(Configuration configuration) {
    return configuration.getProperty(Configuration.SERVICE_TCP_NETTY_READER_IDLE_TIME, Long.class);
  }

  @Provides
  @Singleton
  @Named(Configuration.SERVICE_TCP_NETTY_WRITER_IDLE_TIME)
  Long serviceTcpNettyWriterIdleTimeSeconds(Configuration configuration) {
    return configuration.getProperty(Configuration.SERVICE_TCP_NETTY_WRITER_IDLE_TIME, Long.class);
  }
}
