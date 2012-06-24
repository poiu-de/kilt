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
package org.omnaest.i18nbinder.test;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class I18nClientUserTest
{
  
  @Test
  public void testExecute()
  {
    {
      I18nFacade i18nFacade = new I18nFacade( new Locale( "de", "DE" ) );
      assertEquals( "wert1", i18nFacade.I18n.AdminTest.getMyPropertyKey1() );
      assertEquals( "wert2", i18nFacade.I18n.AdminTest.getMyPropertyKey2() );
      assertEquals( "wert1", i18nFacade.I18n.Sub1.Shared.getMyPropertyKey1() );
      assertEquals( "wert4", i18nFacade.I18n.Sub2.Shared.getMyPropertyKey4() );
    }
    {
      I18nFacade i18nFacade = new I18nFacade( new Locale( "en", "US" ) );
      assertEquals( "value1", i18nFacade.I18n.AdminTest.getMyPropertyKey1() );
      assertEquals( "value2", i18nFacade.I18n.AdminTest.getMyPropertyKey2() );
      assertEquals( "value a and b", i18nFacade.I18n._673numericalTest.getMyPropertyKey1( "a", "b" ) );      
    }
  }
}
