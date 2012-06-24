/*******************************************************************************
 * Copyright 2012 Danny Kunz
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
package org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import javax.annotation.Generated;

import java.util.Map;
import org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n.sub1.Shared;
/**
 * This is an automatically with i18nBinder generated facade class.<br><br>
 * To modify please adapt the underlying property files.<br><br>
 * If the facade class is instantiated with a given {@link Locale} using {@link #Sub1(Locale)} all non static methods will use this predefined {@link Locale} when invoked.<br><br>
 * The facade methods will silently ignore all {@link MissingResourceException}s by default. To alter this behavior see {@link #Sub1(Locale, boolean)}<br><br>
 * Resource base: <b>i18n.sub1</b>
 * @see Shared
 */ 
@Generated(value = "http://code.google.com/p/i18n-binder/", date = "2012-06-24T18:30:49+02:00")
public class Sub1 {
  /** @see Shared */
  public final Shared Shared;

  /**
   * This {@link Sub1} constructor will create a new instance which silently ignores any {@link MissingResourceException} 
   * @see Sub1
   * @param locale
   */ 
  public Sub1( Locale locale )
  {
    this(locale,true);
  }
  

  /**
   * @see Sub1
   * @param locale
   * @param silentlyIgnoreMissingResourceException
   */ 
  public Sub1( Locale locale, boolean silentlyIgnoreMissingResourceException )
  {
    super();
    this.Shared = new Shared( locale, silentlyIgnoreMissingResourceException );
  }
  
}

