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

import java.util.NoSuchElementException;
import org.cfg4j.provider.ConfigurationProvider;

public class ConfigurationImpl implements Configuration {
  private final ConfigurationProvider provider;

  public ConfigurationImpl(ConfigurationProvider provider) {
    this.provider = provider;
  }

  @Override
  public String getNodeName() {
    return getProperty(Configuration.NODE_NAME, String.class);
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

  private <T> T getProperty(String propertyName, Class<T> type) {
    return provider.getProperty(propertyName, type);
  }

  private <T> T getProperty(String propertyName, Class<T> type, T defaultValue) {
    try {
      return provider.getProperty(propertyName, type);
    } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException exception) {
      return defaultValue;
    }
  }
}
