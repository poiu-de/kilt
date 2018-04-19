/*
 * Copyright 2018 mherrn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnaest.i18nbinder.properties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Not thread safe!
 * @author mherrn
 */
public class PropertyFile {
  private final List<Entry> entries= new ArrayList<>();

  private final Map<String, PropertyEntry> propertyEntries= new LinkedHashMap<>();

  public Map<String, String> asMap() {
    final Map<String, String> map= new LinkedHashMap<>();

    for (final Map.Entry<String, PropertyEntry> e : this.propertyEntries.entrySet()) {
      map.put(e.getKey(), e.getValue().getValue());
    }

    return map;
  }


  protected void appendEntry(final BasicEntry entry) {
    this.entries.add(entry);
  }


  protected void appendEntry(final PropertyEntry entry) {
    this.entries.add(entry);
    this.propertyEntries.put(entry.getKey(), entry);
  }


  public void setValue(final String key, final String value) {
    if (this.propertyEntries.containsKey(key)) {
      this.propertyEntries.get(key).setValue(value);
    } else {
      final PropertyEntry entry= new PropertyEntry(key, value);
      this.propertyEntries.put(key, entry);
      this.entries.add(entry);
    }
  }


  public int size() {
    return propertyEntries.size();
  }


  public boolean containsKey(final String key) {
    return this.propertyEntries.containsKey(key);
  }


  public String get(final String key) {
    final PropertyEntry entry= this.propertyEntries.get(key);
    if (entry != null) {
      return entry.getValue();
    } else {
      return null;
    }
  }
}
