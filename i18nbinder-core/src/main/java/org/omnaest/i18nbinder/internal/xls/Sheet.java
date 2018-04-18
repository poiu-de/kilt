package org.omnaest.i18nbinder.internal.xls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.omnaest.i18nbinder.internal.Language;
import org.omnaest.i18nbinder.internal.Translation;


/**
 *
 * @author mherrn
 */
public class Sheet {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  private final String[] fixedColumnsHeaders;
  private final SortedSet<Language> languages= new TreeSet<>();
  private final List<Row> rows= new ArrayList<>();

  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public Sheet(final String... fixedColumnsHeaders) {
    this.fixedColumnsHeaders= fixedColumnsHeaders;
  }

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  public void addLanguage(final Language lang) {
    if (!languages.contains(lang)) {
      languages.add(lang);
    }
  }


  public SortedSet<Language> getLanguages() {
    return languages;
  }


  public void addContentRow(final Row row) {
    this.rows.add(row);
    for (final Translation translation : row.getTranslations()) {
      this.languages.add(translation.getLang());
    }
  }


  private String[] createHeaderRow() {
    final String[] headerRow= new String[this.fixedColumnsHeaders.length + this.languages.size()];
    Arrays.fill(headerRow, "");

    System.arraycopy(this.fixedColumnsHeaders, 0, headerRow, 0, this.fixedColumnsHeaders.length);

    final List<Language> languageList= new ArrayList<>(this.languages);
    for (int i= 0; i < languageList.size(); i++) {
      headerRow[this.fixedColumnsHeaders.length + i]= languageList.get(i).getLang();
    }

    return headerRow;
  }


  public List<String[]> getRows() {
    final List<String[]> allRows= new ArrayList<>(this.rows.size() + 1);
    allRows.add(this.createHeaderRow());
    for (final Row row : this.rows) {
      final String[] stringRow= new String[this.fixedColumnsHeaders.length + this.languages.size()];
      Arrays.fill(stringRow, "");

      stringRow[0]= row.getResourceBundle();
      stringRow[1]= row.getResourceKey();

      for (final Translation translation : row.getTranslations()) {
        final int col= this.fixedColumnsHeaders.length + this.getLanguageIdx(translation.getLang());
        stringRow[col]= translation.getValue();
      }

      allRows.add(stringRow);
    }

    return allRows;
  }


  private int getLanguageIdx(final Language language) {
    return new ArrayList<>(this.languages).indexOf(language);
  }
}
