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
package org.omnaest.i18nbinder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.omnaest.i18nbinder.internal.ModifierHelper;
import org.omnaest.i18nbinder.internal.xls.XLSFile;


public class ExportXlsTask extends Task {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The location to which the generated Java files are written.
   */
  private File outputDirectory;

  /**
   * The location of the source i18n resource bundle files.
   */
  private String propertiesRootDirectory= "i18n";

  private boolean verbose= false;

  private String[] i18nIncludes= new String[]{"**/*.properties"};

  private String[] i18nExcludes= new String[]{};

  /**
   * A regex to filter the resource bundle files for which to generate the Facade(s).
   * <p>
   * For example if you have the following resource bundles:
   * <ul>
   *   <li>messages_de.properties</li>
   *   <li>messages_en.properties</li>
   *   <li>buttons_de.properties</li>
   *   <li>buttons_en.properties</li>
   *   <li>internal/exceptions_de.properties</li>
   *   <li>internal/exceptions_en.properties</li>
   *   <li>internal/messages.properties</li>
   *   <li>internal/messages.properties</li>
   * </ul>
   *
   * and want to generate the facade only for the messages and internal messages,
   * specify <code>.*\/messages_.*\.properties</code>.
   *
   * @see #includeLocaleRegex
   */
  private String includeLocaleRegex;

  /**
   * A regex to filter the resource bundle files for with to generate the Facade(s).
   * <p>
   * For example if you have the following resource bundles:
   * <ul>
   *   <li>messages_de.properties</li>
   *   <li>messages_en.properties</li>
   *   <li>buttons_de.properties</li>
   *   <li>buttons_en.properties</li>
   *   <li>internal/exceptions_de.properties</li>
   *   <li>internal/exceptions_en.properties</li>
   *   <li>internal/messages.properties</li>
   *   <li>internal/messages.properties</li>
   * </ul>
   *
   * and want to avoid the generation of the facade for the internal exceptions,
   * specify <code>internal\/exceptions_.*\.properties</code> here (assuming
   * that the <code>includeLocaleRegex</code> includes all properties files.
   *
   * @see #excludeLocaleRegex
   */
  private String excludeLocaleRegex;

  private String propertyFileEncoding;

  private String xlsFileEncoding= "UTF-8";

  private final List<FileSet> fileSetList = new ArrayList<>();

  private String xlsFileName= null;

  private boolean deleteEmptyProperties= false;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws BuildException {
    if (this.xlsFileName == null) {
      this.log("No xls file name specified. Please provide a file name for the xls file which should be created.", Project.MSG_ERR);
      throw new RuntimeException("No xls file name specified. Please provide a file name for the xls file which should be created.");
    } else {
      this.log("Create XLS file from property files...");

      Set<File> propertyFileSet = this.resolveFilesFromFileSetList(this.fileSetList);

      XLSFile xlsFile = ModifierHelper.createXLSFileFromPropertyFiles(Paths.get(this.propertiesRootDirectory), propertyFileSet, this.propertyFileEncoding,
                                                                      null, null,
                                                                      null,
                                                                      true);

      File file = new File(this.xlsFileName);
      xlsFile.setFile(file);
      xlsFile.store();

      this.log("...done");
    }
  }


  /**
   * @see #resolveFilesFromFileSet(FileSet)
   * @param fileSetList
   * @return
   */
  protected Set<File> resolveFilesFromFileSetList(List<FileSet> fileSetList) {
    Set<File> retset = new HashSet<>();

    if (fileSetList != null) {
      fileSetList.forEach((fileSet) -> {
        retset.addAll(this.resolveFilesFromFileSet(fileSet));
      });
    }

    return retset;
  }


  /**
   * @see #resolveFilesFromFileSetList(List)
   * @param fileSet
   * @return
   */
  protected List<File> resolveFilesFromFileSet(FileSet fileSet) {
    List<File> retlist = new ArrayList<>();

    if (fileSet != null) {
      DirectoryScanner directoryScanner = fileSet.getDirectoryScanner();
      String[] includedFileNames = directoryScanner.getIncludedFiles();

      if (includedFileNames != null) {
        File basedir = directoryScanner.getBasedir();

        for (String fileNameUnnormalized : includedFileNames) {
          String fileName = fileNameUnnormalized.replaceAll(Pattern.quote("\\"), "/");

          File file = new File(basedir, fileName);
          if (file.exists()) {
            retlist.add(file);
          }
        }
      }

    }

    return retlist;
  }


  public void addFileset(FileSet fileset) {
    if (fileset != null) {
      this.fileSetList.add(fileset);
    }
  }


  public String getXlsFileName() {
    return this.xlsFileName;
  }


  public void setXlsFileName(String xlsFileName) {
    this.log("xlsFileName=" + xlsFileName);
    this.xlsFileName = xlsFileName;
  }


  public void setPropertyFileEncoding(String fileEncoding) {
    this.log("propertyFileEncoding=" + fileEncoding);
    this.propertyFileEncoding = fileEncoding;
  }


  public boolean isDeleteEmptyProperties() {
    return this.deleteEmptyProperties;
  }


  public void setDeleteEmptyProperties(boolean deleteEmptyProperties) {
    this.log("deleteEmptyProperties=" + deleteEmptyProperties);
    this.deleteEmptyProperties = deleteEmptyProperties;
  }


  public void setPropertiesRootDirectory(String propertiesRootDirectory) {
    this.log("propertiesRootDirectory=" + propertiesRootDirectory);
    this.propertiesRootDirectory = propertiesRootDirectory;
  }


  public void setXlsFileEncoding(String xlsFileEncoding) {
    this.log("javaXlsEncoding=" + xlsFileEncoding);
    this.xlsFileEncoding = xlsFileEncoding;
  }

}
