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
package de.poiu.kilt.maven;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import de.poiu.kilt.facade.creation.FacadeCreator;
import de.poiu.kilt.bundlecontent.Language;
import de.poiu.kilt.bundlecontent.ResourceBundleContent;
import de.poiu.kilt.bundlecontent.ResourceBundleContentHelper;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


/**
 * Generates the I18n enum facades to allow type safe access to localized messages.
 */
@Mojo(name="create-facade",
      defaultPhase=LifecyclePhase.GENERATE_SOURCES,
      requiresDependencyResolution = ResolutionScope.COMPILE)
@Execute(phase=LifecyclePhase.GENERATE_SOURCES)
public class CreateFacadeMojo extends AbstractKiltMojo {


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The location to which the generated Java files are written.
   */
  @Parameter(property="facadeGenerationDirectory", defaultValue = "${project.build.directory}/generated-sources/kilt", required = true)
  private File facadeGenerationDirectory;


  /**
   * The package name under which the facade(s) will be generated.
   */
  @Parameter(property="generatedPackage", defaultValue="i18n.generated")
  private String generatedPackage;


  // custom encoding of generated java files is not supported at the moment since
  // JavaPoet by default always writes as UTF-8
  // It could be possible to manually write via another charset, but at the moment
  // I don't see any reason to do this
  //@Parameter(property = "javaFileEncoding", defaultValue = "${project.build.sourceEncoding}")
  private String javaFileEncoding;

  /**
   * Whether to copy the facade accessor class and the base interface I18nBundleKey to the
   * generation target dir.
   * This is only useful if it is necessary to avoid a runtime dependency on kilt-runtime.
   */
  @Parameter(property="copyFacadeAccessorClasses", defaultValue= "false")
  private boolean copyFacadeAccessorClasses;

  /**
   * The name of the facade accessor class when copying the facade accessor classes.
   * This is only meaningful in combination with {@link #copyFacadeAccessorClasses}.
   */
  @Parameter(property="facadeAccessorClassName", defaultValue= "I18n")
  private String facadeAccessorClassName;

  /**
   * Whether to execute the generation of the I18n enum facades.
   * <p>
   * Set to <code>true</code> to skip the generation.
   */
  @Parameter(property = "skip", defaultValue = "false")
  private boolean skipFacadeGeneration;



  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws MojoExecutionException {
    if (this.verbose) {
      Configurator.setLevel(LogManager.getLogger("de.poiu.kilt").getName(), Level.DEBUG);
    }

    if (this.skipFacadeGeneration) {
      this.getLog().info("Skipping to create the i18n Java facade as requested in the configuration");
    } else {
      final FileMatcher fileMatcher= new FileMatcher(this.propertiesRootDirectory.toPath(), this.i18nIncludes, this.i18nExcludes);
      final Set<File> propertyFiles= fileMatcher.findMatchingFiles();
      this.getLog().info("Creating facade for the following files: "+propertyFiles);

      try {
        // generate the the enum facade(s)
        final ResourceBundleContentHelper fbcHelper = new ResourceBundleContentHelper(fileMatcher.getRoot());
        final Map<String, Map<Language, File>> bundleNameToFilesMap = fbcHelper.toBundleNameToFilesMap(propertyFiles);

        final FacadeCreator facadeCreator = new FacadeCreator();
        for (final Map.Entry<String, Map<Language, File>> entry : bundleNameToFilesMap.entrySet()) {
          final String bundleName = entry.getKey();
          final Map<Language, File> bundleTranslations = entry.getValue();

          final ResourceBundleContent resourceBundleContent = ResourceBundleContent.forName(bundleName).fromFiles(bundleTranslations);
          final TypeSpec resourceBundleEnumTypeSpec = facadeCreator.createFacadeEnumFor(resourceBundleContent);
          final JavaFile javaFile = JavaFile.builder(generatedPackage, resourceBundleEnumTypeSpec).build();
          javaFile.writeTo(facadeGenerationDirectory);
          // TODO: To allow for custom charsets, we need to call javaFile.toString.getBytes(Charset), but this involves
          //       creating the directoy structure and identifying the correct file name.
        }

        // copy the facade accessor classes if requested
        if (copyFacadeAccessorClasses) {
          facadeCreator.copyFacadeAccessorTemplates(facadeAccessorClassName, generatedPackage, facadeGenerationDirectory.toPath());
        }

        this.project.addCompileSourceRoot(this.facadeGenerationDirectory.getCanonicalPath());
      } catch (IOException e) {
        this.getLog().error("Could not write Java facade to file", e);
      }

      this.getLog().info("...done");
    }
  }
}
