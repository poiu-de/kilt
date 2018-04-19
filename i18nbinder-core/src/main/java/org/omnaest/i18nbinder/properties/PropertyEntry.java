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


/**
 *
 * @author mherrn
 */
public class PropertyEntry implements Entry {
  //FIXME: Wenn wir trailingKeyContent und leadingValueContent dem Separator zuschlagen, könnten wir
  //       dort flexibler agieren *z.B. nur ein Leerzeichen als Trenner
  private String leadingKeyContent;
  private String key;
  private String separator;
  private String value;
  private String trailingValueContent;


  public PropertyEntry(String key, String value) {
    this.key = key;
    this.value = value;
  }


  protected PropertyEntry(final String leadingKeyContent, final String key, final String separator, final String value, final String trailingValueContent) {
    this.leadingKeyContent= leadingKeyContent;
    this.key= key;
    this.separator= separator;
    this.value= value;
    this.trailingValueContent= trailingValueContent;
  }


  @Override
  public String asString() {
    return leadingKeyContent
      + key
      + separator
      + value
      + trailingValueContent;
  }


  public String getKey() {
    return this.key;
  }

  public String getValue() {
    return this.value;
  }

  public void setValue(final String value) {
    this.value= value;
  }
}
