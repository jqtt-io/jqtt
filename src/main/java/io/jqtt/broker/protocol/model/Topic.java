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

import io.jqtt.exception.ParseTopicException;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true, includeFieldNames = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Topic implements Serializable {
  private static final String SEPARATOR = "/";
  private static final long serialVersionUID = 1085380342957502397L;

  @ToString.Include private final String topic;

  @EqualsAndHashCode.Include private transient List<Segment> tokens;

  public Topic(String topic) throws ParseTopicException {
    this.topic = topic;
    this.tokens = new ArrayList<>();

    tokenize();
  }

  public int size() {
    return tokens.size();
  }

  private void tokenize() throws ParseTopicException {
    if (Objects.isNull(topic)) {
      throw new ParseTopicException("Segment cannot be null.");
    }

    if (topic.isEmpty()) {
      throw new ParseTopicException("Segment cannot be empty.");
    }

    final List<String> parts = new ArrayList<>(Arrays.asList(topic.split(SEPARATOR)));
    if (topic.endsWith("/")) {
      parts.add("");
    }

    final int size = parts.size();
    final int last = size - 1;
    for (int index = 0; index < size; index++) {
      Segment element = new Segment(parts.get(index));
      if (Segment.EMPTY.value().equals(element)) {
        tokens.add(element);
      } else if (Segment.MULTI.equals(element)) {
        if (index != last) {
          throw new ParseTopicException("Wildcard # should be on last position in topic.");
        }
        tokens.add(Segment.MULTI);
      } else if (element.value().contains(Segment.EMPTY.value())) {
        throw new ParseTopicException(
            "#  is illegal character in topic segment. Should be on last position only.");
      } else if (Segment.SINGLE.equals(element)) {
        tokens.add(Segment.SINGLE);
      } else if (element.value().contains(Segment.SINGLE.value())) {
        throw new ParseTopicException(
            "+ is illegal character in topic segment. Should be separate part of segments.");
      } else {
        tokens.add(element);
      }
    }
  }
}
