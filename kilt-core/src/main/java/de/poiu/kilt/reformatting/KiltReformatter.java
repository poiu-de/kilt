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
package de.poiu.kilt.reformatting;

import de.poiu.apron.ApronOptions;
import de.poiu.apron.MissingKeyAction;
import de.poiu.apron.PropertyFile;
import de.poiu.apron.UnicodeHandling;
import de.poiu.apron.reformatting.AttachCommentsTo;
import de.poiu.apron.reformatting.InvalidFormatException;
import de.poiu.apron.reformatting.ReformatOptions;
import de.poiu.apron.reformatting.Reformatter;
import de.poiu.fez.Require;
import de.poiu.kilt.bundlecontent.RememberingPropertyFile;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Reformat .properties files.
 *
 * @author mherrn
 */
public class KiltReformatter {

  private static final Logger LOGGER= LogManager.getLogger();

  private static final ApronOptions APRON_OPTIONS= ApronOptions.create()
        .with(MissingKeyAction.NOTHING)
        .with(UnicodeHandling.DO_NOTHING);

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
   * Reformats the key-value pairs in the given list of .properties files according to the given
   * format string.
   * <p>
   * The given format string must conform to the following specification:
   * <ul>
   *  <li>It <i>may</i> contain some leading whitespace before the key.</li>
   *  <li>It <i>must</i> contain the string <code>&lt;key&gt;</code> to indicate the position of the
   *      properties key (case doesn't matter)</li>
   *  <li>It <i>must</i> contain a separator char (either a colon or an equals sign) which <i>may</i>
   *      be surrounded by some whitespace characters.</li>
   *  <li>It <i>must</i> contain the string <code>&lt;value&gt;</code> to indicate the position of the
   *      properties value (case doesn't matter)</li>
   *  <li>It <i>must</i> contain the line ending char(s) (either <code>\n</code> or <code>\r</code>
   *      or <code>\r\n</code>)</li>
   * </ul>
   * The allowed whitespace characters are the space character, the tab character and the linefeed character.
   * <p>
   * Therefore a typical format string is
   * <pre>
   * &lt;key&gt; = &lt;value&gt;\n
   * </pre>
   * for
   * <ul>
   *  <li>no leading whitespace</li>
   *  <li>an equals sign as separator surrounded by a single whitespace character on each side</li>
   *  <li><code>\n</code> as the line ending char.</li>
   * </ul>
   * But it may as well be
   * <pre>
   * \t \f&lt;key&gt;\t: &lt;value&gt;\r\n
   * </pre>
   * for a rather strange format with
   * <ul>
   *  <li>a tab, a whitespace and a linefeed char as leading whitespace</li>
   *  <li>a colon as separator char preceded by a tab and followed a single space character</li>
   *  <li><code>\r\n</code> as the line ending chars
   * </ul>
   * <p>
   * The parameter <code>reformatKeyAndValue</code> specifies whether the key and value will be
   * reformatted by removing obsolete whitespace and newline characters.
   *
   * @param fileMatcher fileMatcher for the files to reformat
   * @param formatString the format string specifying how to format the key-value pairs
   * @param reformatKeyAndValue whether to reformat the key and value by stripping away all unnecessary whitespace and linebreaks
   * @param charset the charset to use for reading and writing the .properties files
   * @throws InvalidFormatException if the given format string is invalid
   */
  public void reformat(final FileMatcher fileMatcher, final String formatString, final boolean reformatKeyAndValue, final Charset charset) {
    Require.nonNull(fileMatcher);
    Require.nonNull(formatString);
    Require.nonNull(charset);

    final Set<File> propertyFiles= fileMatcher.findMatchingFiles();
    LOGGER.log(Level.INFO, "Reformatting entries in the following files: {}", propertyFiles);

    final Reformatter reformatter= new Reformatter(
      ReformatOptions.create()
        .with(charset)
        .withFormat(formatString)
        .withReformatKeyAndValue(reformatKeyAndValue));

    for (final File propertyFile : propertyFiles) {
      reformatter.reformat(propertyFile);
    }
  }


  /**
   * Reorders the entries in the given list of .properties files alphabetically the the names of their keys.
   *
   * @param fileMatcher fileMatcher for the files to reorder
   * @param attachCommentsTo how to handle BasicEntries (comments and empty lines) when reordering
   * @param charset the charset to use for reading and writing the .properties files
   */
  public void reorderByKey(final FileMatcher fileMatcher, final AttachCommentsTo attachCommentsTo, final Charset charset) {
    Require.nonNull(fileMatcher);
    Require.nonNull(attachCommentsTo);
    Require.nonNull(charset);

    final Reformatter reformatter= new Reformatter(
      ReformatOptions.create()
        .with(charset)
        .with(attachCommentsTo));

    final Set<File> propertyFiles= fileMatcher.findMatchingFiles();
    LOGGER.log(Level.INFO, "Reordering entries in the following files: {}", propertyFiles);

    propertyFiles.stream().forEach(_f -> {
      final PropertyFile pf= PropertyFile.from(_f, charset);
      reformatter.reorderByKey(pf);
      pf.overwrite(_f, APRON_OPTIONS.with(charset));
    });
  }


  /**
   * Reorders the entries in the given list of .properties files in the same order as in the given template.
   *
   * @param template the reference for the order of the entries
   * @param fileMatcher fileMatcher for the files to reorder
   * @param attachCommentsTo how to handle BasicEntries (comments and empty lines) when reordering
   * @param charset the charset to use for reading and writing the .properties files
   */
  public void reorderByTemplate(final File template, final FileMatcher fileMatcher, final AttachCommentsTo attachCommentsTo, final Charset charset) {
    Require.nonNull(fileMatcher);
    Require.nonNull(attachCommentsTo);
    Require.nonNull(charset);

    final Set<File> propertyFiles= fileMatcher.findMatchingFiles();
    LOGGER.log(Level.INFO, "Reordering entries by template {} in the following files: {}", template, propertyFiles);

    // create a PropertyFile instance for each file
    final PropertyFile reference= PropertyFile.from(template, charset);
    final Set<RememberingPropertyFile> pfs= propertyFiles.stream()
      .filter(_f -> {
        if (_f.getAbsolutePath().equals(template.getAbsolutePath())) {
          LOGGER.log(Level.DEBUG, "Ignoring property file "+_f.getAbsolutePath()+" because it is the same as the reference template.");
          return false;
        } else {
          return true;
        }
      })
      .map(_f -> {
        return RememberingPropertyFile.from(_f, charset);
      })
      .collect(Collectors.toSet());

//    // and reorder in the given order
//    for (int i= 1; i < pfs.size(); i++) {
//      final RememberingPropertyFile rpf= pfs.get(i);
//      new Reformatter().reorderByTemplate(pfs.get(i - 1).propertyFile, rpf.propertyFile, attachCommentsTo);
//      rpf.propertyFile.overwrite(rpf.actualFile);
//    }

    // and reorder them
    final Reformatter reformatter= new Reformatter(
    ReformatOptions.create()
      .with(charset)
      .with(attachCommentsTo));

    for (final RememberingPropertyFile rpf : pfs) {
      reformatter.reorderByTemplate(reference, rpf.propertyFile);
      rpf.propertyFile.overwrite(rpf.actualFile, APRON_OPTIONS.with(charset));
    }
  }
}
