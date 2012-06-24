package org.omnaest.i18nbinder.internal.facade.i18nfacade;

import java.util.Locale;
import java.util.MissingResourceException;
import javax.annotation.Generated;

import java.util.Map;
import org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n._673numericalTest;
import org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n.AdminTest;
import org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n.LocalelessTest;
import org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n.Sub1;
import org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n.Sub2;
import org.omnaest.i18nbinder.internal.facade.i18nfacade.i18n.ViewTest;
/**
 * This is an automatically with i18nBinder generated facade class.<br><br>
 * To modify please adapt the underlying property files.<br><br>
 * If the facade class is instantiated with a given {@link Locale} using {@link #I18n(Locale)} all non static methods will use this predefined {@link Locale} when invoked.<br><br>
 * The facade methods will silently ignore all {@link MissingResourceException}s by default. To alter this behavior see {@link #I18n(Locale, boolean)}<br><br>
 * Resource base: <b>i18n</b>
 * @see _673numericalTest
 * @see AdminTest
 * @see LocalelessTest
 * @see Sub1
 * @see Sub2
 * @see ViewTest
 */ 
@Generated(value = "http://code.google.com/p/i18n-binder/", date = "2012-06-24T18:23:10+02:00")
public class I18n {
  /** @see _673numericalTest */
  public final _673numericalTest _673numericalTest;
  /** @see AdminTest */
  public final AdminTest AdminTest;
  /** @see LocalelessTest */
  public final LocalelessTest LocalelessTest;
  /** @see Sub1 */
  public final Sub1 Sub1;
  /** @see Sub2 */
  public final Sub2 Sub2;
  /** @see ViewTest */
  public final ViewTest ViewTest;

  /**
   * This {@link I18n} constructor will create a new instance which silently ignores any {@link MissingResourceException} 
   * @see I18n
   * @param locale
   */ 
  public I18n( Locale locale )
  {
    this(locale,true);
  }
  

  /**
   * @see I18n
   * @param locale
   * @param silentlyIgnoreMissingResourceException
   */ 
  public I18n( Locale locale, boolean silentlyIgnoreMissingResourceException )
  {
    super();
    this._673numericalTest = new _673numericalTest( locale, silentlyIgnoreMissingResourceException );
    this.AdminTest = new AdminTest( locale, silentlyIgnoreMissingResourceException );
    this.LocalelessTest = new LocalelessTest( locale, silentlyIgnoreMissingResourceException );
    this.Sub1 = new Sub1( locale, silentlyIgnoreMissingResourceException );
    this.Sub2 = new Sub2( locale, silentlyIgnoreMissingResourceException );
    this.ViewTest = new ViewTest( locale, silentlyIgnoreMissingResourceException );
  }
  
}

