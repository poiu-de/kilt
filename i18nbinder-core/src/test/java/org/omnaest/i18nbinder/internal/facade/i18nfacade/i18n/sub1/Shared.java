package org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n.sub1;

import java.util.Locale;
import java.util.MissingResourceException;
import javax.annotation.Generated;

import java.util.Map;
import org.omnaest.i18nbinder.internal.facade.I18nFacade;
import org.omnaest.i18nbinder.internal.facade.I18nFacade.Translator;
/**
 * This is an automatically with i18nBinder generated facade class.<br><br>
 * To modify please adapt the underlying property files.<br><br>
 * If the facade class is instantiated with a given {@link Locale} using {@link #Shared(Locale)} all non static methods will use this predefined {@link Locale} when invoked.<br><br>
 * The facade methods will silently ignore all {@link MissingResourceException}s by default. To alter this behavior see {@link #Shared(Locale, boolean)}<br><br>
 * Resource base: <b>i18n.sub1.shared</b>
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
 * <td rowspan="1">my.property.key1</td>
 * <td>de_DE=wert1</td>
 * </tr>
 * <tr>
 * <td rowspan="1">my.property.key2</td>
 * <td>de_DE=wert2</td>
 * </tr>
 * </tbody>
 * </table><br><br>
 * @see #translator()
 * @see #translator(Locale)
 */ 
@Generated(value = "http://code.google.com/p/i18n-binder/", date = "2012-09-07T20:48:42+02:00")
public class Shared {
  public final static String baseName = "i18n.sub1.shared";
  private final Locale locale;
  private final boolean silentlyIgnoreMissingResourceException;

  /**
   * This {@link Shared} constructor will create a new instance which silently ignores any {@link MissingResourceException} 
   * @see Shared
   * @param locale
   */ 
  public Shared( Locale locale )
  {
    this(locale,true);
  }
  

  /**
   * @see Shared
   * @param locale
   * @param silentlyIgnoreMissingResourceException
   */ 
  public Shared( Locale locale, boolean silentlyIgnoreMissingResourceException )
  {
    super();
    this.locale = locale;
    this.silentlyIgnoreMissingResourceException = silentlyIgnoreMissingResourceException;
  }
  
  /**
   * Similar to {@link #getMyPropertyKey1()} for the given {@link Locale}.
   * @see Shared
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
   * </ul>
   * @see Shared
   */ 
  public String getMyPropertyKey1()
  {
    return getMyPropertyKey1( this.locale );
  }

  /**
   * Similar to {@link #getMyPropertyKey2()} for the given {@link Locale}.
   * @see Shared
   * @see #getMyPropertyKey2()
   * @param locale 
   */ 
  protected String getMyPropertyKey2(Locale locale)
  {
    try
    {
      final String key = "my.property.key2";
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
   * Returns the value of the property key <b>my.property.key2</b> for the predefined {@link Locale}.
   * <br><br>
   * 
   * Examples:
   * <ul>
   * <li>de_DE=wert2</li>
   * </ul>
   * @see Shared
   */ 
  public String getMyPropertyKey2()
  {
    return getMyPropertyKey2( this.locale );
  }

  /**
   * Returns a new instance of {@link Shared} which uses the given setting for the exception handling
   * @see Shared
   * @param silentlyIgnoreMissingResourceException 
   */ 
  public Shared doSilentlyIgnoreMissingResourceException( boolean silentlyIgnoreMissingResourceException )
  {
    return new Shared( this.locale, silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new instance of {@link Shared} which uses the given {@link Locale}
   * @see Shared
   * @param locale 
   */ 
  public Shared forLocale( Locale locale )
  {
    return new Shared( locale, this.silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the given {@link Locale} and based on the {@value #baseName} i18n base
   * @see Shared
   * @see #translator()
   * @see #translator(Locale)
   * @return {@link Translator}   */ 
  public static Translator translator(Locale locale, boolean silentlyIgnoreMissingResourceException)
  {
    return new Translator( baseName, locale, silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the given {@link Locale} and based on the {@value #baseName} i18n base
   * @see Shared
   * @see #translator()
   * @see #translator(Locale,boolean)
   * @return {@link Translator}   */ 
  public Translator translator(Locale locale)
  {
    return new Translator( baseName, locale, this.silentlyIgnoreMissingResourceException );
  }

  /**
   * Returns a new {@link Translator} instance using the internal {@link Locale} and based on the {@value #baseName} i18n base
   * @see Shared
   * @see #translator(Locale)
   * @see #translator(Locale,boolean)
   * @return {@link Translator}   */ 
  public Translator translator()
  {
    return translator( this.locale );
  }

}

