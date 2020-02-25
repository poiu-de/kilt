/*
 * Copyright (C) 2020 Marco Herrn
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

import java.util.Map;


/**
 *
 * @author mherrn
 */
public class EnhancedMap<K, V> {
  private final Map<K, V> map;

  public EnhancedMap(final Map<K, V> map) {
    this.map= map;
  }

  /**
   * If the given key exists in the map, must return its value. Otherwise put the given value and
   * return it again.
   * <p>
   * This is a behaviour that is more consistent (and logical) to the behaviour of
   * {@link Map#computeIfAbsent(java.lang.Object, java.util.function.Function)} that also returns
   * the value that will be associated to the key after the operation finishes.
   * <p>
   * This method is just a wrapper around {@link Map#putIfAbsent(java.lang.Object, java.lang.Object)}
   * that doesn't return <code>null</code> (unless the stored value is <code>null</code>), but instead
   * the value that will be stored in the map after this method finishes.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return the previous value associated with the specified key, or the given value if there was no mapping for that key.
   */
  public V putIfAbsentAndGet(final K key, final V value) {
    final V previousValue = this.map.putIfAbsent(key, value);
    if (previousValue != null) {
      return previousValue;
    } else {
      return value;
    }
  }


  /**
   * Returns the Map this EnhancedMap encloses. This will be the actual map, not a copy. Any
   * modifications to the returned map will actually modify the enclosed map.
   *
   * @return the enclosed map
   */
  public Map<K, V> innerMap() {
    return this.map;
  }
}
