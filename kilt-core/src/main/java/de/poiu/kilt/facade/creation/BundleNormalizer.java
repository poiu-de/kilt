/*
 * Copyright (C) 2018 Marco Herrn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.poiu.kilt.facade.creation;

import com.google.common.collect.ImmutableMap;
import de.poiu.fez.Require;
import java.text.Normalizer;
import java.util.Map;
import java.util.logging.*;

/**
 * Normalizes key and bundle names to valid strings to be used as class
 * names, enum names, etc.
 *
 * @author mherrn
 */
public class BundleNormalizer {
  private static final Logger LOGGER= Logger.getLogger(BundleNormalizer.class.getName());

  /**
   * Certain characters will be replaced by multiple ascii characters, for example
   * ä is replaced by ae.
   */
  private static final Map<String, String> UMLAUT_REPLACEMENTS= ImmutableMap.<String, String>builder()
          .put("ä", "ae")
          .put("Ä", "AE")
          .put("ö", "oe")
          .put("Ö", "OE")
          .put("ü", "ue")
          .put("Ü", "UE")
          .put("ß", "ss")
          .put("ẞ", "SS")
          .put("Æ", "AE")
          .put("Œ", "OE")
          .build();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Normalizes a string to be used as a class name.
   * This actually converts it to UpperCamelCase and prepends it with an underscore
   * if it would otherwise start with a number.
   *
   * @param s the string to convert
   * @return the converted string
   */
  public static String toClassName(String s) {
    Require.nonNull(s, "parameter s may not be null or empty.");
    Require.nonWhitespace(s, "parameter s may not be null or empty.");

    s= normalize(s);

    final char[] chars= s.toCharArray();
    final StringBuilder sb= new StringBuilder();

    //if the first char is a number, prepend it by an underscore
    if (Character.isDigit(chars[0])) {
      sb.append("_");
    }

    //always uppercase the first char
    sb.append(Character.toUpperCase(chars[0]));

    for (int i=1; i<chars.length; i++) {
      if (chars[i] == '_') {
        // if char is underscore, leave it out…
        if (chars.length > i+1) {
          // … and uppercase next letter, if available
          final char nextChar= chars[i+1];
          sb.append(Character.toUpperCase(nextChar));
          i++;
        }
      } else {
        // leave all other chars as the are
        sb.append(chars[i]);
      }
    }

    return sb.toString();
  }


  /**
   * Normalizes a string to be used as a field name.
   * This actually converts it to lowerCamelCase and prepends it with an underscore
   * if it would otherwise start with a number.
   *
   * @param s the string to convert
   * @return the converted string
   */
  public static String toFieldName(String s) {
    Require.nonNull(s, "parameter s may not be null or empty.");
    Require.nonWhitespace(s, "parameter s may not be null or empty.");

    s= normalize(s);

    final char[] chars= s.toCharArray();
    final StringBuilder sb= new StringBuilder();

    //if the first char is a number, prepend it by an underscore
    if (Character.isDigit(chars[0])) {
      sb.append("_");
    }

    //always lowercase the first char
    sb.append(Character.toLowerCase(chars[0]));

    for (int i=1; i<chars.length; i++) {
      if (chars[i] == '_') {
        // if char is underscore, leave it out…
        if (chars.length > i+1) {
          // … and uppercase next letter, if available
          final char nextChar= chars[i+1];
          sb.append(Character.toUpperCase(nextChar));
          i++;
        }
      } else {
        // leave all other chars as the are
        sb.append(chars[i]);
      }
    }

    return sb.toString();
  }


  /**
   * Normalizes a string to be used as a field name.
   * This actually converts it to ALL_UPPER_CASE and prepends it with an underscore
   * if it would otherwise start with a number.
   *
   * @param s the string to convert
   * @return the converted string
   */
  public static String toConstName(String s) {
    Require.nonNull(s, "parameter s may not be null or empty.");
    Require.nonWhitespace(s, "parameter s may not be null or empty.");

    s= normalize(s);

    final char[] chars= s.toCharArray();
    final StringBuilder sb= new StringBuilder();

    //if the first char is a number, prepend it by an underscore
    if (Character.isDigit(chars[0])) {
      sb.append("_");
    }

    // Uppercase all chars
    for (int i=0; i<chars.length; i++) {
      sb.append(Character.toUpperCase(chars[i]));
    }

    return sb.toString();
  }


  /**
   * Normalizes a string to be used as a resource bundle name.
   * This actually converts all slashes to dots.
   *
   * @param s the string to convert
   * @return the converted string
   */
  public static String toBundleName(String s) {
    Require.nonNull(s, "parameter s may not be null or empty.");
    Require.nonWhitespace(s, "parameter s may not be null or empty.");

    return s.replaceAll("\\/", ".");
  }


  /**
   * Normalizes a string to be used as java identifier.
   * <p>
   * This actually removes, replaces or simplifies certain characters, for example
   * the character 'ä' would be replaced by 'ae' which is usual habit in Germany.
   * <p>
   * Other characters, like 'ÿ' are simplified by removing the diacritics and therefore
   * will be replaced by 'y'.
   * <p>
   * Multiple underscores will be reduced to only one underscore and all leading and trailing
   * underscores removed.
   *
   * @param s the string to normalize
   * @return the normalized string
   */
  protected static String normalize(String s) {
    Require.nonNull(s, "parameter s may not be null or empty.");

    // first replace umlauts with two-characters
    s= replaceUmlauts(s);

    // then replace some special characters
    s= s.replaceAll("[\\ \\.\\-\n/]+", "_");

    // then remove all diacritics and all characters not US_ASCII or numbers
    s= Normalizer
            .normalize(s, Normalizer.Form.NFKD)
            .replaceAll("[^\\P{M}]", "")
            .replaceAll("[^\\p{Alnum}\\_\\-\\ ]", "");

    // then replace multiple underscores with only one underscore
    s= s.replaceAll("[_]+", "_");

    // remove all leading and trailing underscores
    s= s.replaceAll("^_", "").replaceAll("_$", "");

    return s;
  }


  /**
   * Replaces certain Umlauts according to the mapping in {@link #UMLAUT_REPLACEMENTS}.
   *
   * @param s the string whose umlauts to replace
   * @return the string with the replaced umlauts
   */
  protected static String replaceUmlauts(final String s) {
    String result= s;
    for (final Map.Entry<String, String> entry : UMLAUT_REPLACEMENTS.entrySet()) {
      final String character= entry.getKey();
      final String replacement = entry.getValue();
      result= result.replaceAll(character, replacement);
    }
    return result;
  }
}
