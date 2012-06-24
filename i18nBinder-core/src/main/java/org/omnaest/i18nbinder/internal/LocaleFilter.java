/*******************************************************************************
 * Copyright 2011 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.omnaest.i18nbinder.internal;

import java.util.regex.Pattern;

/**
 * Locale filter which allows to easily filter locales. As default all locales are accepted.
 * 
 * @author Omnaest
 */
public class LocaleFilter
{
  /* ********************************************** Constants ********************************************** */
  private static final String LOCALE_FILTER_PATTERN_STRING_DEFAULT = ".*";
  
  /* ********************************************** Variables ********************************************** */
  protected Pattern           pattern                              = Pattern.compile( LOCALE_FILTER_PATTERN_STRING_DEFAULT );
  
  /* ********************************************** Methods ********************************************** */

  public boolean isLocaleAccepted( String locale )
  {
    return this.pattern.matcher( locale ).matches();
  }
  
  public void setPattern( Pattern pattern )
  {
    this.pattern = pattern;
  }
  
  public Pattern getPattern()
  {
    return this.pattern;
  }
  
}
