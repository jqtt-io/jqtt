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

package io.jqtt.broker.protocol.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jqtt.exception.ParseTopicException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TopicTest {
  @Test
  public void shouldRejectNullTopic() {
    assertThrows(ParseTopicException.class, () -> new Topic(null));
  }

  @ParameterizedTest(name = "run #{index} with [{arguments}]")
  @ValueSource(strings = {"", "topic1#/", "topic1+/", "topic1//", "+"})
  void shouldRejectInvalidTopic(String topic) {
    assertThrows(ParseTopicException.class, () -> new Topic(topic));
  }

  @ParameterizedTest(name = "run #{index} with [{arguments}]")
  @ValueSource(strings = {"sendor/+/celsius/#"})
  void shouldAcceptInvalidTopic(String topic) throws ParseTopicException {
    assertThat(new Topic(topic).toString()).isEqualTo("Topic(#)");
  }
}
