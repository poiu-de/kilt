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
package de.poiu.kilt.ant;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import de.poiu.kilt.internal.Language;
import de.poiu.kilt.internal.ResourceBundleContent;
import de.poiu.kilt.internal.ResourceBundleContentHelper;
import de.poiu.kilt.facade.creation.FacadeCreator;


/**
 * Creates the I18n enum facades for type safe access to localized
 * messages.
 */
public class CreateFacadeTask extends Task {

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

  /**
   * The package name under which the facade(s) will be generated.
   */
  private String generatedPackage= "i18n.generated";

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

  private String javaFileEncoding= "UTF-8";

  /**
   * Whether to copy the facade accessor class and the base interface I18nBundleKey to the
   * generation target dir.
   * This is only useful if it is necessary to avoid a runtime dependency on kilt-runtime.
   */
  private boolean copyFacadeAccessorClasses= false;

  /**
   * The name of the facade accessor class when copying the facade accessor classes.
   * This is only meaningful in combination with {@link #copyFacadeAccessorClasses}.
   */
  private String facadeAccessorClassName= "I18n";

  /**
   * Whether to execute the generation of the I18n enum facades.
   * <p>
   * Set to <code>true</code> to skip the generation.
   */
  private boolean skipFacadeGeneration= false;

  private final List<FileSet> fileSetList = new ArrayList<>();

  private String xlsFileName= null;

  private boolean deleteEmptyProperties= false;

  private Path facadeGenerationDir = Paths.get("generated-sources");


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws BuildException {
    this.log("Create Java source code facade file from property files.");

    final Set<File> propertyFileSet = this.resolveFilesFromFileSetList(this.fileSetList);

    try {
      final ResourceBundleContentHelper fbcHelper = new ResourceBundleContentHelper(Paths.get(propertiesRootDirectory));
      final Map<String, Map<Language, File>> bundleNameToFilesMap = fbcHelper.toBundleNameToFilesMap(propertyFileSet);

      final FacadeCreator facadeCreator = new FacadeCreator();
      for (final Map.Entry<String, Map<Language, File>> entry : bundleNameToFilesMap.entrySet()) {
        final String bundleName = entry.getKey();
        final Map<Language, File> bundleTranslations = entry.getValue();

        final ResourceBundleContent resourceBundleContent = ResourceBundleContent.forName(bundleName).fromFiles(bundleTranslations);
        final TypeSpec resourceBundleEnumTypeSpec = facadeCreator.createFacadeEnumFor(resourceBundleContent);
        final JavaFile javaFile = JavaFile.builder(generatedPackage, resourceBundleEnumTypeSpec).build();
        javaFile.writeTo(facadeGenerationDir);
      }

      if (copyFacadeAccessorClasses) {
        facadeCreator.copyFacadeAccessorTemplates(facadeAccessorClassName, generatedPackage, facadeGenerationDir);
      }
    } catch (IOException e) {
      this.log("Could not write Java facade to file", e, Project.MSG_ERR);
      throw new RuntimeException(e);
    }

    this.log("...done");
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


  public void setFileEncoding(String fileEncoding) {
    this.log("fileEncoding=" + fileEncoding);
    this.propertyFileEncoding = fileEncoding;
    this.javaFileEncoding = fileEncoding;
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


  public void setGeneratedPackage(String generatedPackage) {
    de.poiu.kilt.util.Objects.requireNonWhitespace(generatedPackage, "generatedPackage may not be empty");
    this.log("packageName=" + generatedPackage);
    this.generatedPackage = generatedPackage;
  }


  public void setPropertyFileEncoding(String propertyFileEncoding) {
    this.log("propertyFileEncoding=" + propertyFileEncoding);
    this.propertyFileEncoding = propertyFileEncoding;
  }


  public void setJavaFileEncoding(String javaFileEncoding) {
    this.log("javaFileEncoding=" + javaFileEncoding);
    this.javaFileEncoding = javaFileEncoding;
  }


  public void setCopyFacadeAccessorClasses(final boolean copyFacadeAccessorClasses) {
    this.log("copyFacadeAccessorClasses=" + copyFacadeAccessorClasses);
    this.copyFacadeAccessorClasses = copyFacadeAccessorClasses;
  }


  public void setFacadeAccessorClassName(final String facadeAccessorClassName) {
    this.log("facadeAccessorClassName=" + facadeAccessorClassName);
    this.facadeAccessorClassName = facadeAccessorClassName;
  }


  public void setFacadeGenerationDir(final String facadeGenerationDir) {
    this.log("facadeGenerationDir=" + facadeGenerationDir);
    this.facadeGenerationDir = Paths.get(facadeGenerationDir);
  }
}
