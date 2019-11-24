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
package de.poiu.kilt.cli;

import com.google.common.base.Joiner;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import de.poiu.kilt.facade.creation.FacadeCreator;
import de.poiu.kilt.bundlecontent.Language;
import de.poiu.kilt.bundlecontent.ResourceBundleContent;
import de.poiu.kilt.bundlecontent.ResourceBundleContentHelper;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 *
 * @author mherrn
 */
@Command(name = "create-facade",
         description= "Generates the I18N enum facades to allow type safe access to localized messages.",
         sortOptions = false)
public class KiltCreateFacade extends AbstractKiltCommand implements Runnable {

  private static final Logger LOGGER= LogManager.getLogger();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The location to which the generated Java files are written.
   */
  @Option(names={"-o", "--outputDirectory"}, description= "The location to which the generated Java files are written. (default: ${DEFAULT-VALUE})")
  private Path outputDirectory= Paths.get("generated-sources");


  /**
   * The package name under which the facade(s) will be generated.
   */
  @Option(names={"-p", "--package", "--generatedPackage"}, description= "The package name under which the facade(s) will be generated. (default: ${DEFAULT-VALUE})")
  private String generatedPackage= "i18n.generated";


  /**
   * The encoding of the generated java files.
   */
  //@Option(names= {"--jenc", "--javaFileEncoding"}, description= "The encoding of the generated java files. (default: ${DEFAULT-VALUE})")
  private String javaFileEncoding= "UTF-8";


  /**
   * Whether to copy the facade accessor class and the base interface I18nBundleKey to the
   * generation target dir.
   * This is only useful if it is necessary to avoid a runtime dependency on kilt-runtime.
   */
  @Option(names={"-c", "--copyFacadeAccessorClasses"}, description= "Whether to copy the facade accessor class and the base interface I18nBundleKey to the generation target dir. (default: ${DEFAULT-VALUE})")
  private boolean copyFacadeAccessorClasses= false;


  /**
   * The name of the facade accessor class when copying the facade accessor classes.
   * This is only meaningful in combination with {@link #copyFacadeAccessorClasses}.
   */
  @Option(names={"-n", "--facadeAccessorClassName"}, description= "The name of the facade accessor class when copying the facade accessor classes. (default: ${DEFAULT-VALUE})")
  private String facadeAccessorClassName= "I18n";



  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public KiltCreateFacade() {
    super();
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void run() {
    if (this.verbose) {
      printProperties();
    }

    final Set<File> propertyFileSet = new FileMatcher(this.propertiesRootDirectory, i18nIncludes, i18nExcludes).findMatchingFiles();
    LOGGER.log(Level.INFO, "Creating facade for the following files: "+propertyFileSet);

    try {
      final ResourceBundleContentHelper fbcHelper = new ResourceBundleContentHelper(propertiesRootDirectory);
      final Map<String, Map<Language, File>> bundleNameToFilesMap = fbcHelper.toBundleNameToFilesMap(propertyFileSet);

      final FacadeCreator facadeCreator = new FacadeCreator();
      for (final Map.Entry<String, Map<Language, File>> entry : bundleNameToFilesMap.entrySet()) {
        final String bundleName = entry.getKey();
        final Map<Language, File> bundleTranslations = entry.getValue();

        final ResourceBundleContent resourceBundleContent = ResourceBundleContent.forName(bundleName).fromFiles(bundleTranslations);
        final TypeSpec resourceBundleEnumTypeSpec = facadeCreator.createFacadeEnumFor(resourceBundleContent);
        final JavaFile javaFile = JavaFile.builder(generatedPackage, resourceBundleEnumTypeSpec).build();
        javaFile.writeTo(outputDirectory);
      }

      if (copyFacadeAccessorClasses) {
        facadeCreator.copyFacadeAccessorTemplates(facadeAccessorClassName, generatedPackage, outputDirectory);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not write Java facade to file", e);
    }
  }


  private void printProperties(){
    final StringBuilder sb= new StringBuilder();

    sb.append("verbose                   = ").append(this.verbose).append("\n");
    sb.append("propertiesRootDirectory   = ").append(this.propertiesRootDirectory).append("\n");
    sb.append("i18nIncludes              = ").append(Joiner.on(", ").join(this.i18nIncludes)).append("\n");
    sb.append("i18nExcludes              = ").append(Joiner.on(", ").join(this.i18nExcludes)).append("\n");
    sb.append("propertyFileEncoding      = ").append(this.propertyFileEncoding).append("\n");
    sb.append("outputDirectory           = ").append(this.outputDirectory.toAbsolutePath()).append("\n");
    sb.append("generatedPackage          = ").append(this.generatedPackage).append("\n");
    //sb.append("javaFileEncoding          = ").append(this.javaFileEncoding).append("\n");
    sb.append("copyFacadeAccessorClasses = ").append(this.copyFacadeAccessorClasses).append("\n");
    sb.append("facadeAccessorClassName   = ").append(this.facadeAccessorClassName).append("\n");

    System.out.println(sb.toString());
  }
}
