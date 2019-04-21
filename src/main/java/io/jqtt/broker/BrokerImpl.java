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

package io.jqtt.broker;

import io.jqtt.broker.entrypoint.EntrypointComposition;
import io.jqtt.broker.protocol.authenticator.AuthenticatorFactory;
import io.jqtt.broker.protocol.message.MessageHandlerFactory;
import io.jqtt.configuration.Configuration;
import io.jqtt.exception.JqttExcepion;
import java.lang.reflect.InvocationTargetException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrokerImpl implements Broker {
  private final Configuration configuration;
  private final EntrypointComposition entrypoints;

  public BrokerImpl(Configuration configuration, EntrypointComposition entrypoints) {
    this.configuration = configuration;
    this.entrypoints = entrypoints;
  }

  @Override
  public void start() throws JqttExcepion {
    entrypoints.tcpSocketEntrypoint().start(initializeMessageHandlerFactory());
  }

  private AuthenticatorFactory initializeAuthenticatorFactory() {
    final @NonNull String authenticatorClass = configuration.getAuthenticatorClass();

    return loadClass(
        authenticatorClass, AuthenticatorFactory.class, Configuration.class, configuration);
  }

  private MessageHandlerFactory initializeMessageHandlerFactory() {
    return new MessageHandlerFactory(initializeAuthenticatorFactory(), configuration);
  }

  private <T, U> T loadClass(
      String className, Class<T> intrface, Class<U> constructorArgClass, U props) {
    T instance;
    try {
      log.info(
          "Invoking constructor with {} argument. ClassName={}, interfaceName={}",
          constructorArgClass.getName(),
          className,
          intrface.getName());
      instance =
          this.getClass()
              .getClassLoader()
              .loadClass(className)
              .asSubclass(intrface)
              .getConstructor(constructorArgClass)
              .newInstance(props);
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
      log.warn(
          "Unable to invoke constructor with {} argument. ClassName={}, interfaceName={}, cause={}, "
              + "errorMessage={}",
          constructorArgClass.getName(),
          className,
          intrface.getName(),
          ex.getCause(),
          ex.getMessage());
      return null;
    } catch (NoSuchMethodException | InvocationTargetException e) {
      try {
        log.info(
            "Invoking default constructor. ClassName={}, interfaceName={}",
            className,
            intrface.getName());
        instance =
            this.getClass()
                .getClassLoader()
                .loadClass(className)
                .asSubclass(intrface)
                .getDeclaredConstructor()
                .newInstance();
      } catch (InstantiationException
          | IllegalAccessException
          | ClassNotFoundException
          | NoSuchMethodException
          | InvocationTargetException ex) {
        log.error(
            "Unable to invoke default constructor. ClassName={}, interfaceName={}, cause={}, "
                + "errorMessage={}",
            className,
            intrface.getName(),
            ex.getCause(),
            ex.getMessage());
        return null;
      }
    }

    return instance;
  }
}
