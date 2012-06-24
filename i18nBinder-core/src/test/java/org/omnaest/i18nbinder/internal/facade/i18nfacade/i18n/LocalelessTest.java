package org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import javax.annotation.Generated;

import java.util.Map;
import org.omnaest.i18nbinder.internal.facade.I18nFacade;
import org.omnaest.i18nbinder.internal.facade.I18nFacade.Translator;
/**
 * This is an automatically with i18nBinder generated facade class.<br><br>
 * To modify please adapt the underlying property files.<br><br>
 * If the facade class is instantiated with a given {@link Locale} using {@link #LocalelessTest(Locale)} all non static methods will use this predefined {@link Locale} when invoked.<br><br>
 * The facade methods will silently ignore all {@link MissingResourceException}s by default. To alter this behavior see {@link #LocalelessTest(Locale, boolean)}<br><br>
 * Resource base: <b>i18n.localelessTest</b>
 * <br><br>
 * <h1>Examples:</h1>
 * <table border="1">
 * <thead>
 * <tr>
 * <th>key</th>
 * <th>examples</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td rowspan="1">my.property.key9</td>
 * <td>=value9</td>
 * </tr>
 * </tbody>
 * </table><br><br>
 * @see #translator()
 * @see #translator(Locale)
 */ 
@Generated(value = "org.omnaest.i18nbinder.I18nBinder", date = "2012-06-24T10:55:42+02:00")
public class LocalelessTest {
  public final static String baseName = "i18n.localelessTest";
  private final Locale locale;
  private final boolean silentlyIgnoreMissingResourceException;

  /**
   * This {@link LocalelessTest} constructor will create a new instance which silently ignores any {@link MissingResourceException} 
   * @see LocalelessTest
   * @param locale
   */ 
  public LocalelessTest( Locale locale )
  {
    this(locale,true);
  }
  

  /**
   * @see LocalelessTest
   * @param locale
   * @param silentlyIgnoreMissingResourceException
   */ 
  public LocalelessTest( Locale locale, boolean silentlyIgnoreMissingResourceException )
  {
    super();
    this.locale = locale;
    this.silentlyIgnoreMissingResourceException = silentlyIgnoreMissingResourceException;
  }
  
  /**
   * Similar to {@link #getMyPropertyKey9()} for the given {@link Locale}.
   * @see LocalelessTest
   * @see #getMyPropertyKey9()
   * @param locale 
   */ 
  protected String getMyPropertyKey9(Locale locale)
  {
    try
    {
      final String key = "my.property.key9";
      return I18nFacade.Resource.resourceBasedTranslator.translate( baseName, key, locale );
    }
    catch ( MissingResourceException e )
    {
      if (!this.silentlyIgnoreMissingResourceException)
      {
        throw e;
      }
      return null;
    }
  }

  /**
   * Returns the value of the property key <b>my.property.key9</b> for the predefined {@link Locale}.
   * <br><br>
   * 
   * Examples:
   * <ul>
   * <li>=value9</li>
   * </ul>
   * @see LocalelessTest
   */ 
  public String getMyPropertyKey9()
  {
    return getMyPropertyKey9( this.locale );
  }

  /**
   * Returns a new instance of {@link LocalelessTest} which uses the given setting for the exception handling
   * @see LocalelessTest
   * @param silentlyIgnoreMissingResourceException 
   */ 
  public LocalelessTest doSilentlyIgnoreMissingResourceException( boolean silentlyIgnoreMissingResourceException )
  {
    return new LocalelessTest( this.locale, silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new instance of {@link LocalelessTest} which uses the given {@link Locale}
   * @see LocalelessTest
   * @param locale 
   */ 
  public LocalelessTest forLocale( Locale locale )
  {
    return new LocalelessTest( locale, this.silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the given {@link Locale} and based on the {@value #baseName} i18n base
   * @see LocalelessTest
   * @see #translator()
   * @see #translator(Locale)
   * @return {@link Translator}   */ 
  public static Translator translator(Locale locale, boolean silentlyIgnoreMissingResourceException)
  {
    return new Translator( baseName, locale, silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the given {@link Locale} and based on the {@value #baseName} i18n base
   * @see LocalelessTest
   * @see #translator()
   * @see #translator(Locale,boolean)
   * @return {@link Translator}   */ 
  public Translator translator(Locale locale)
  {
    return new Translator( baseName, locale, this.silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the internal {@link Locale} and based on the {@value #baseName} i18n base
   * @see LocalelessTest
   * @see #translator(Locale)
   * @see #translator(Locale,boolean)
   * @return {@link Translator}   */ 
  public Translator translator()
  {
    return translator( this.locale );
  }

}

