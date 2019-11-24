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
import de.poiu.fez.Require;
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;


/**
 * Creates the I18n enum facades for type safe access to localized
 * messages.
 */
public class CreateFacadeTask extends Task {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The location of the source i18n resource bundle files.
   */
  private String propertiesRootDirectory= "i18n";

  private boolean verbose= false;


  private String propertyFileEncoding;

  //private String javaFileEncoding= "UTF-8";

  private Path facadeGenerationDirectory = Paths.get("generated-sources");

  /**
   * The package name under which the facade(s) will be generated.
   */
  private String generatedPackage= "i18n.generated";

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

  private final List<FileSet> fileSetList = new ArrayList<>();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws BuildException {
    if (this.verbose) {
      Configurator.setLevel(LogManager.getLogger("de.poiu.kilt").getName(), Level.DEBUG);
    }

    if (this.verbose) {
      printProperties();
    }

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
        javaFile.writeTo(facadeGenerationDirectory);
      }

      if (copyFacadeAccessorClasses) {
        facadeCreator.copyFacadeAccessorTemplates(facadeAccessorClassName, generatedPackage, facadeGenerationDirectory);
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


//  public void setFileEncoding(String fileEncoding) {
//    this.propertyFileEncoding = fileEncoding;
//    this.javaFileEncoding = fileEncoding;
//  }



  public void setPropertiesRootDirectory(String propertiesRootDirectory) {
    this.propertiesRootDirectory = propertiesRootDirectory;
  }


  public void setGeneratedPackage(String generatedPackage) {
    Require.nonWhitespace(generatedPackage, "generatedPackage may not be empty");
    this.generatedPackage = generatedPackage;
  }


  public void setPropertyFileEncoding(String propertyFileEncoding) {
    this.propertyFileEncoding = propertyFileEncoding;
  }


//  public void setJavaFileEncoding(String javaFileEncoding) {
//    this.javaFileEncoding = javaFileEncoding;
//  }


  public void setCopyFacadeAccessorClasses(final boolean copyFacadeAccessorClasses) {
    this.copyFacadeAccessorClasses = copyFacadeAccessorClasses;
  }


  public void setFacadeAccessorClassName(final String facadeAccessorClassName) {
    this.facadeAccessorClassName = facadeAccessorClassName;
  }


  public void setFacadeGenerationDir(final String facadeGenerationDir) {
    this.facadeGenerationDirectory = Paths.get(facadeGenerationDir);
  }


  public void setVerbose(final boolean verbose) {
    this.verbose= verbose;
  }


  private void printProperties(){
    final StringBuilder sb= new StringBuilder();

    sb.append("verbose                  = ").append(this.verbose).append("\n");
    sb.append("propertiesRootDirectory   = ").append(this.propertiesRootDirectory).append("\n");
    sb.append("i18nIncludes              = ").append(this.fileSetList).append("\n");
    sb.append("propertyFileEncoding      = ").append(this.propertyFileEncoding).append("\n");
    sb.append("facadeGenerationDirectory = ").append(this.facadeGenerationDirectory).append("\n");
    sb.append("generatedPackage          = ").append(this.generatedPackage).append("\n");
    sb.append("copyFacadeAccessorClasses = ").append(this.copyFacadeAccessorClasses).append("\n");
    sb.append("facadeAccessorClassName   = ").append(this.facadeAccessorClassName).append("\n");

    System.out.println(sb.toString());
  }
}
