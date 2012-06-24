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
