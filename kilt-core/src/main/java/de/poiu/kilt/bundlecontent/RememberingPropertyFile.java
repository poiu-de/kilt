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
package de.poiu.kilt.bundlecontent;

import de.poiu.apron.PropertyFile;
import java.io.File;
import java.nio.charset.Charset;


/**
 * Wraps a {@link PropertyFile} and a corresponding {@link File} object.
 *
 * @author mherrn
 */
public class RememberingPropertyFile {

  /** The file the PropertyFile was created from. */
  public final File actualFile;

  /** The PropertyFile that was read from {@link #actualFile}. */
  public final PropertyFile propertyFile;


  /**
   * Creates a new RememberingPropertyFile with the given File and PropertyFile.
   * <p>
   * This only creates a new RememberingPropertyFile with the given values. It does not read
   * the contents of the given file into the given PropertyFile. To create a RememberingPropertyFile
   * by reading in a file use {@link #from(java.io.File) } instead.
   *
   * @param file the file the PropertyFile was created from
   * @param propertyFile the PropertyFile that was read from the given file
   */
  public RememberingPropertyFile(final File file, final PropertyFile propertyFile) {
    this.actualFile = file;
    this.propertyFile = propertyFile;
  }


  /**
   * Reads a PropertyFile from the given file and builds a RememberingPropertyFile from it
   * encapsulating the read file and the created PropertyFile.
   * <p>
   * This methods assumes the encoding of the file to be UTF-8.
   *
   * @param file the .properties file to read
   * @return a RememberingPropertyFile for the given file
   */
  public static RememberingPropertyFile from(final File file) {
    final PropertyFile propertyFile= PropertyFile.from(file);
    return new RememberingPropertyFile(file, propertyFile);
  }


  /**
   * Reads a PropertyFile from the given file.
   * <p>
   * The .properties file must be in the correct encoding of the given charset.
   *
   * @param file the .properties file to read
   * @param charset the encoding of the .properties file
   * @return a RememberingPropertyFile for the given file
   */
  public static RememberingPropertyFile from(final File file, final Charset charset) {
    final PropertyFile propertyFile= PropertyFile.from(file, charset);
    return new RememberingPropertyFile(file, propertyFile);
  }

}
