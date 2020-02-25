/*
 * Copyright (C) 2019 Marco Herrn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.poiu.kilt.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author mherrn
 */
public class EnhancedMapTest {

  /**
   * This class is used in the test to differentiate two equal Strings.
   */
  private static class StringRef {
    private final String s;

    public StringRef(final String s) {
      this.s= s;
    }
  }


  /**
   * Check that StringRef really differentiates between two equal Strings.
   */
  @Test
  public void testStringRef() {
    final StringRef sRef1= new StringRef("myString");
    final StringRef sRef2= new StringRef("myString");

    assertThat(sRef1).isNotSameAs(sRef2);
  }


  @Test
  public void testPutIfAbsendAndGet_isAbsent() {
    // - preparation

    final Map<String, StringRef> innerMap= new HashMap<>();
    final EnhancedMap<String, StringRef> eMap= new EnhancedMap<>(innerMap);

    final StringRef sRef1= new StringRef("eins");
    final StringRef sRef2= new StringRef("zwei");

    innerMap.put("one", sRef1);
    innerMap.put("two", sRef2);

    final StringRef newRef1= new StringRef("eins");
    final StringRef newRef2= new StringRef("zwei");
    final StringRef newRef3= new StringRef("drei");

    // - test

    final StringRef result1= eMap.putIfAbsentAndGet("one", newRef1);
    final StringRef result2= eMap.putIfAbsentAndGet("two", newRef2);
    final StringRef result3= eMap.putIfAbsentAndGet("three", newRef3);

    // - verification

    assertThat(result1).isSameAs(sRef1);
    assertThat(result1).isNotSameAs(newRef1);

    assertThat(result2).isSameAs(sRef2);
    assertThat(result2).isNotSameAs(newRef2);

    assertThat(result3).isSameAs(result3);
  }

}
