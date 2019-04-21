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

package io.jqtt.broker.handler;

import io.jqtt.broker.protocol.message.MessageHandler;
import io.jqtt.broker.protocol.message.MessageHandlerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.NonNull;

public class MqttServerHandler extends ChannelInboundHandlerAdapter {

  private final MessageHandler connectMessageHandler;

  public MqttServerHandler(final @NonNull MessageHandlerFactory messageHandlerFactory) {
    this.connectMessageHandler = messageHandlerFactory.fromMessage("CONNECT");
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    final MqttMessage message = (MqttMessage) msg;

    switch (message.fixedHeader().messageType()) {
      case CONNECT:
        ctx.writeAndFlush(connectMessageHandler.handle(message, ctx));
        break;
      case PINGREQ:
      default:
        break;
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
