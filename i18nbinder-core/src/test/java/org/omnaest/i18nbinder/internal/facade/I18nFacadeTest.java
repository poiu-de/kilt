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
package org.omnaest.i18nbinder.internal.facade;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.omnaest.i18nbinder.internal.facade.I18nFacade.ResourceBasedTranslator;
import org.omnaest.utils.structure.map.MapBuilder;

public class I18nFacadeTest
{
  
  @Test
  public void testInstantiation()
  {
    Locale locale = Locale.US;
    I18nFacade i18nFacade = new I18nFacade( locale );
    assertEquals( "value {0} and {1}", i18nFacade.I18n._673numericalTest.getMyPropertyKey1() );
    assertEquals( "value3 with {arbitrary} replacement", i18nFacade.I18n._673numericalTest.getMyPropertyKey3() );
    assertEquals( "value2", i18nFacade.I18n.AdminTest.getMyPropertyKey2() );
    
    //
    assertEquals( "value a and b", i18nFacade.I18n._673numericalTest.getMyPropertyKey1( "a", "b" ) );
    
    //
    Map<String, String> placeholderToReplacementMap = new HashMap<String, String>();
    placeholderToReplacementMap.put( "arbitrary", "another" );
    assertEquals( "value3 with another replacement",
                  i18nFacade.I18n._673numericalTest.getMyPropertyKey3( placeholderToReplacementMap ) );
    
    //
    assertEquals( "value {0} and {1}", i18nFacade.I18n._673numericalTest.translator().translate( "my.property.key1" ) );
    
    //
    {
      //
      String[] keys = i18nFacade.I18n._673numericalTest.translator().allPropertyKeys();
      Map<String, String> map = i18nFacade.I18n._673numericalTest.translator().translate( keys );
      assertEquals( Arrays.asList( "my.property.key1", "my.property.key3" ), new ArrayList<String>( map.keySet() ) );
      assertEquals( Arrays.asList( "value {0} and {1}", "value3 with {arbitrary} replacement" ),
                    new ArrayList<String>( map.values() ) );
    }
    
  }
  
  @Test
  public void testSimilarNamedResources()
  {
    Locale locale = Locale.GERMAN;
    I18nFacade i18nFacade = new I18nFacade( locale );
    assertEquals( "wert1", i18nFacade.I18n.Sub1.Shared.getMyPropertyKey1() );
    assertEquals( "wert3", i18nFacade.I18n.Sub2.Shared.getMyPropertyKey3() );
  }
  
  @Test(expected = MissingResourceException.class)
  public void testTranslateWithMissingKey()
  {
    //
    Locale locale = Locale.US;
    I18nFacade i18nFacade = new I18nFacade( locale, false );
    assertEquals( null, i18nFacade.I18n._673numericalTest.translator().translate( "non.existing.key" ) );
  }
  
  @Test
  public void testTryTranslateWithMissingKey()
  {
    //
    Locale locale = Locale.US;
    I18nFacade i18nFacade = new I18nFacade( locale );
    assertEquals( null, i18nFacade.I18n._673numericalTest.translator().translate( "non.existing.key" ) );
  }
  
  @Test(expected = MissingResourceException.class)
  public void testTranslationMapWithMissingKey()
  {
    //
    Locale locale = Locale.US;
    I18nFacade i18nFacade = new I18nFacade( locale, false );
    
    //
    String[] keys = ArrayUtils.add( i18nFacade.I18n._673numericalTest.translator().allPropertyKeys(), 0, "missingKey" );
    Map<String, String> map = i18nFacade.I18n._673numericalTest.translator().translate( keys );
    assertEquals( Arrays.asList( "my.property.key1", "my.property.key3" ), new ArrayList<String>( map.keySet() ) );
    assertEquals( Arrays.asList( "value {0} and {1}", "value3 with {arbitrary} replacement" ),
                  new ArrayList<String>( map.values() ) );
    
  }
  
  @Test
  public void testSilentlyIgnoringExceptionTranslationMapWithMissingKey()
  {
    //
    Locale locale = Locale.US;
    I18nFacade i18nFacade = new I18nFacade( locale );
    
    //
    String[] keys = ArrayUtils.add( i18nFacade.I18n._673numericalTest.forLocale( locale ).translator().allPropertyKeys(), 0,
                                    "missingKey" );
    Map<String, String> map = i18nFacade.I18n._673numericalTest.translator().translate( keys );
    assertEquals( Arrays.asList( "missingKey", "my.property.key1", "my.property.key3" ), new ArrayList<String>( map.keySet() ) );
    assertEquals( Arrays.asList( null, "value {0} and {1}", "value3 with {arbitrary} replacement" ),
                  new ArrayList<String>( map.values() ) );
    
  }
  
  @Test
  public void testMockingTheFacade()
  {
    //
    I18nFacade.use( new ResourceBasedTranslator()
    {
      @Override
      public String translate( String baseName, String key, Locale locale )
      {
        return new MapBuilder<String, String>().linkedHashMap()
                                               .put( "a", "value a" )
                                               .put( "b", "value b" )
                                               .put( "c", "value c" )
                                               .build()
                                               .get( key );
      }
      
      @Override
      public String[] resolveAllKeys( String baseName, Locale locale )
      {
        return new String[] { "a", "b", "c" };
      }
    } );
    
    //
    final I18nFacade i18nFacade = new I18nFacade( Locale.US );
    assertEquals( "value b", i18nFacade.I18n.AdminTest.translator().translate( "b" ) );
    assertEquals( "value b", i18nFacade.I18n.LocalelessTest.translator().translate( "b" ) );
    assertArrayEquals( new String[] { "a", "b", "c" }, i18nFacade.I18n.LocalelessTest.translator()
                                                                                     .translationMap()
                                                                                     .keySet()
                                                                                     .toArray( new String[0] ) );
  }
}
