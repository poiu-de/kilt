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
 * If the facade class is instantiated with a given {@link Locale} using {@link #ViewTest(Locale)} all non static methods will use this predefined {@link Locale} when invoked.<br><br>
 * The facade methods will silently ignore all {@link MissingResourceException}s by default. To alter this behavior see {@link #ViewTest(Locale, boolean)}<br><br>
 * Resource base: <b>i18n.viewTest</b>
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
 * <td rowspan="2">my.property.key1</td>
 * <td>de_DE=wert1</td>
 * </tr>
 * <tr>
 * <td><small>en_US=value1</small></td>
 * </tr>
 * <tr>
 * <td rowspan="1">my.property.key3</td>
 * <td>en_US=value3</td>
 * </tr>
 * <tr>
 * <td rowspan="1">my.property.key4</td>
 * <td>de_DE=wert4</td>
 * </tr>
 * </tbody>
 * </table><br><br>
 * @see #translator()
 * @see #translator(Locale)
 */ 
@Generated(value = "http://code.google.com/p/i18n-binder/", date = "2012-06-24T15:21:48+02:00")
public class ViewTest {
  public final static String baseName = "i18n.viewTest";
  private final Locale locale;
  private final boolean silentlyIgnoreMissingResourceException;

  /**
   * This {@link ViewTest} constructor will create a new instance which silently ignores any {@link MissingResourceException} 
   * @see ViewTest
   * @param locale
   */ 
  public ViewTest( Locale locale )
  {
    this(locale,true);
  }
  

  /**
   * @see ViewTest
   * @param locale
   * @param silentlyIgnoreMissingResourceException
   */ 
  public ViewTest( Locale locale, boolean silentlyIgnoreMissingResourceException )
  {
    super();
    this.locale = locale;
    this.silentlyIgnoreMissingResourceException = silentlyIgnoreMissingResourceException;
  }
  
  /**
   * Similar to {@link #getMyPropertyKey1()} for the given {@link Locale}.
   * @see ViewTest
   * @see #getMyPropertyKey1()
   * @param locale 
   */ 
  protected String getMyPropertyKey1(Locale locale)
  {
    try
    {
      final String key = "my.property.key1";
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
   * Returns the value of the property key <b>my.property.key1</b> for the predefined {@link Locale}.
   * <br><br>
   * 
   * Examples:
   * <ul>
   * <li>de_DE=wert1</li>
   * <li>en_US=value1</li>
   * </ul>
   * @see ViewTest
   */ 
  public String getMyPropertyKey1()
  {
    return getMyPropertyKey1( this.locale );
  }

  /**
   * Similar to {@link #getMyPropertyKey3()} for the given {@link Locale}.
   * @see ViewTest
   * @see #getMyPropertyKey3()
   * @param locale 
   */ 
  protected String getMyPropertyKey3(Locale locale)
  {
    try
    {
      final String key = "my.property.key3";
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
   * Returns the value of the property key <b>my.property.key3</b> for the predefined {@link Locale}.
   * <br><br>
   * 
   * Examples:
   * <ul>
   * <li>en_US=value3</li>
   * </ul>
   * @see ViewTest
   */ 
  public String getMyPropertyKey3()
  {
    return getMyPropertyKey3( this.locale );
  }

  /**
   * Similar to {@link #getMyPropertyKey4()} for the given {@link Locale}.
   * @see ViewTest
   * @see #getMyPropertyKey4()
   * @param locale 
   */ 
  protected String getMyPropertyKey4(Locale locale)
  {
    try
    {
      final String key = "my.property.key4";
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
   * Returns the value of the property key <b>my.property.key4</b> for the predefined {@link Locale}.
   * <br><br>
   * 
   * Examples:
   * <ul>
   * <li>de_DE=wert4</li>
   * </ul>
   * @see ViewTest
   */ 
  public String getMyPropertyKey4()
  {
    return getMyPropertyKey4( this.locale );
  }

  /**
   * Returns a new instance of {@link ViewTest} which uses the given setting for the exception handling
   * @see ViewTest
   * @param silentlyIgnoreMissingResourceException 
   */ 
  public ViewTest doSilentlyIgnoreMissingResourceException( boolean silentlyIgnoreMissingResourceException )
  {
    return new ViewTest( this.locale, silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new instance of {@link ViewTest} which uses the given {@link Locale}
   * @see ViewTest
   * @param locale 
   */ 
  public ViewTest forLocale( Locale locale )
  {
    return new ViewTest( locale, this.silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the given {@link Locale} and based on the {@value #baseName} i18n base
   * @see ViewTest
   * @see #translator()
   * @see #translator(Locale)
   * @return {@link Translator}   */ 
  public static Translator translator(Locale locale, boolean silentlyIgnoreMissingResourceException)
  {
    return new Translator( baseName, locale, silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the given {@link Locale} and based on the {@value #baseName} i18n base
   * @see ViewTest
   * @see #translator()
   * @see #translator(Locale,boolean)
   * @return {@link Translator}   */ 
  public Translator translator(Locale locale)
  {
    return new Translator( baseName, locale, this.silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the internal {@link Locale} and based on the {@value #baseName} i18n base
   * @see ViewTest
   * @see #translator(Locale)
   * @see #translator(Locale,boolean)
   * @return {@link Translator}   */ 
  public Translator translator()
  {
    return translator( this.locale );
  }

}

