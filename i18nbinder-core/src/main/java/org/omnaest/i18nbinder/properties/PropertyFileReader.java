/*
 * Copyright 2018 mherrn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnaest.i18nbinder.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;


/**
 *
 * @author mherrn
 */
public class PropertyFileReader {
  protected static final Pattern PATTERN_PROPERTY_LINE= Pattern.compile("^"
    + "(?<LEADINGSPACE>\\s*)"
    + "(?<KEY>\\w+)"
    + "(?<SEPARATOR>\\s*[=:\\s]\\s*)"
    + "(?<VALUE>.*?)"
    + "(?<TRAILINGSPACE>\\s*)"
    + "$", UNICODE_CHARACTER_CLASS);


  public static PropertyFile read(final File file) {
    final PropertyFile propertyFile= new PropertyFile();

    try (final BufferedReader reader= new BufferedReader(new FileReader(file));){
      String line;
      while((line= reader.readLine()) != null) {
        final Matcher matcher = PATTERN_PROPERTY_LINE.matcher(line);
        if (matcher.matches()) {
          // key-value-line
          final String leadingSpace= matcher.group("LEADINGSPACE");
          final String key= matcher.group("KEY");
          final String separator= matcher.group("SEPARATOR");
          final String value;

          // value may be split over multiple lines
          // therfore we need to read the next lines until no further continuation is necessary
          if (matcher.group("VALUE").endsWith("\\")) {
            String continuationLine= "";
            while (line.endsWith("\\")) {
              line= reader.readLine();
              continuationLine += line.replaceFirst("^\\s+", "").replaceFirst("\\s+$", "");
            }
            value= matcher.group("VALUE").replaceFirst("\\$", "") + continuationLine;
          } else {
            value= matcher.group("VALUE");
          }

          // what about the trailing whitespace? Can we ignore it?

          propertyFile.appendEntry(new PropertyEntry(leadingSpace, key, separator, value, ""));
        } else {
          // comment or empty line
          propertyFile.appendEntry(new BasicEntry(line));
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    return propertyFile;
  }
}
