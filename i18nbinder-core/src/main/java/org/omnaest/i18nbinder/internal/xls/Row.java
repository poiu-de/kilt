
package org.omnaest.i18nbinder.internal.xls;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContent.Translation;

/**
 *
 * @author mherrn
 */
public class Row {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  private String resourceBundle;
  private String resourceKey;
  private final SortedSet<Translation> translations= new TreeSet<>();

  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public Row() {

  }

  public Row(final String resourceBundle) {
    this.resourceBundle= resourceBundle;
  }

  public Row(final String resourceBundle, final String resourceKey) {
    this.resourceBundle= resourceBundle;
    this.resourceKey= resourceKey;
  }

  public Row(final String resourceBundle, final String resourceKey, final Collection<Translation> translations) {
    this.resourceBundle= resourceBundle;
    this.resourceKey= resourceKey;
    this.translations.addAll(translations);
  }

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  public void addTranslation(final Translation translation) {
    this.translations.add(translation);
  }


  public String getResourceBundle() {
    return resourceBundle;
  }


  public void setResourceBundle(String resourceBundle) {
    this.resourceBundle = resourceBundle;
  }


  public String getResourceKey() {
    return resourceKey;
  }


  public void setResourceKey(String resourceKey) {
    this.resourceKey = resourceKey;
  }


  public SortedSet<Translation> getTranslations() {
    return translations;
  }

}
