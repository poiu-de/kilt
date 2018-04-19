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
package org.maven.i18nbinder.plugin;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.omnaest.i18nbinder.internal.Language;
import org.omnaest.i18nbinder.internal.ResourceBundleContent;
import org.omnaest.i18nbinder.internal.ResourceBundleContentHelper;
import org.omnaest.i18nbinder.facade.creation.FacadeCreator;


/**
 * Generates the I18N enum Facades to allow type safe access to localized messages.
 */
@Mojo(name="create-facade",
      defaultPhase=LifecyclePhase.GENERATE_SOURCES,
      requiresDependencyResolution = ResolutionScope.COMPILE)
@Execute(phase=LifecyclePhase.GENERATE_SOURCES)
public class CreateFacadeMojo extends AbstractMojo {


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The location to which the generated Java files are written.
   */
  @Parameter(property="outputDirectory", defaultValue = "${project.build.directory}/generated-sources/i18nbinder", required = true)
  private File outputDirectory;

  /**
   * The location of the source i18n resource bundle files.
   */
  @Parameter(property="propertiesRootDirectory", defaultValue="src/main/resources/i18n", required=true)
  private File propertiesRootDirectory;

  @Parameter(property="verbose", defaultValue="false")
  private boolean verbose;

  /**
   * The package name under which the facade(s) will be generated.
   */
  @Parameter(property="generatedPackage", defaultValue="i18n.generated")
  private String generatedPackage;

  //FIXME: Should be taken from jaxb2 maven plugin
  @Parameter(property="i18nIncludes", defaultValue="**/*.properties")
  private String[] i18nIncludes;

  @Parameter(property="i18nExcludes")
  private String[] i18nExcludes;

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
//  @Parameter(defaultValue=".*")
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
//  @Parameter
  private String excludeLocaleRegex;

  @Parameter(property = "propertyFileEncoding")
  private String propertyFileEncoding;

  // custom encoding of generated java files is not supported at the moment since
  // JavaPoet by default always writes as UTF-8
  // It could be possible to manually write via another charset, but at the moment
  // I don't see any reason to do this
  @Parameter(property = "javaFileEncoding", defaultValue = "${project.build.sourceEncoding}")
  private String javaFileEncoding;

  /**
   * Whether to copy the facade accessor class and the base interface I18nBundleKey to the
   * generation target dir.
   * This is only useful if it is necessary to avoid a runtime dependency on i18nbinder-runtime.
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



  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws MojoExecutionException {
    if (this.skipFacadeGeneration) {
      this.getLog().info("Skipping to create the i18n Java facade as requested in the configuration");
    } else {
      this.getLog().info("Create Java source code facade file from property files.");
      this.logConfigurationProperties();
      final Set<File> propertyFileSet = this.resolveFilesFromDirectoryRoot(this.propertiesRootDirectory);

      try {
        // generate the the enum facade(s)
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
          // TODO: To allow for custom charsets, we need to call javaFile.toString.getBytes(Charset), but this involves
          //       creating the directoy structure and identifying the correct file name.
        }

        // copy the facade accessor classes if requested
        if (copyFacadeAccessorClasses) {
          facadeCreator.copyFacadeAccessorTemplates(facadeAccessorClassName, generatedPackage, outputDirectory.toPath());
        }

        this.project.addCompileSourceRoot(this.outputDirectory.getCanonicalPath());
      } catch (IOException e) {
        this.getLog().error("Could not write Java facade to file", e);
      }

      this.getLog().info("...done");
    }
  }


  private void logConfigurationProperties() {
    this.getLog().info("createJavaFacade=" + this.skipFacadeGeneration);
    this.getLog().info("javaFileEncoding=" + this.javaFileEncoding);
    this.getLog().info("includeLocaleRegex=" + this.includeLocaleRegex);
    this.getLog().info("excludeLocaleRegex=" + this.excludeLocaleRegex);
    this.getLog().info("i18nIncludes" + Arrays.toString(this.i18nIncludes));
    this.getLog().info("i18nExcludes" + Arrays.toString(this.i18nExcludes));
    this.getLog().info("outputDirectory=" + this.outputDirectory);
    this.getLog().info("generatedPackage=" + this.generatedPackage);
    this.getLog().info("propertiesRootDirectory=" + this.propertiesRootDirectory);
  }


  private Set<File> resolveFilesFromDirectoryRoot(File propertiesRootDirectory) {
    if (!propertiesRootDirectory.exists()) {
      this.getLog().warn("resource bundle directory "+propertiesRootDirectory+" does not exist. No enum facades will be generated.");
      return ImmutableSet.of();
    }

    final Set<File> matchingFiles= new LinkedHashSet<>();

    final DirectoryScanner directoryScanner = new DirectoryScanner();
    directoryScanner.setIncludes(this.i18nIncludes);
    directoryScanner.setExcludes(this.i18nExcludes);
    directoryScanner.setBasedir(propertiesRootDirectory);
    directoryScanner.scan();

    final String[] fileNames= directoryScanner.getIncludedFiles();
    for (String fileName : fileNames) {
      if (this.verbose) {
        this.getLog().info("Including in facade: " + fileName);
      }
      matchingFiles.add(new File(propertiesRootDirectory, fileName));
    }

    if (matchingFiles.isEmpty()) {
      this.getLog().warn("No resource bundles found. No enum facades will be generated.");
    }

    return matchingFiles;
  }
}
