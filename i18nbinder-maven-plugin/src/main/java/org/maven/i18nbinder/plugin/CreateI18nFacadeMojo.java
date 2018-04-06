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

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.omnaest.i18nbinder.internal.LocaleFilter;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContent;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContent.Language;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContentHelper;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeCreator;


/**
 * Generates the I18N enum Facades to allow type safe access to localized messages.
 */
@Mojo(name="generateI18nFacade",
      defaultPhase=LifecyclePhase.GENERATE_SOURCES,
      requiresDependencyResolution = ResolutionScope.COMPILE)
@Execute(phase=LifecyclePhase.GENERATE_SOURCES)
public class CreateI18nFacadeMojo extends AbstractMojo {


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The location to which the generated Java files are written.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-sources/i18nbinder", required = true)
  private File outputDirectory;

  /**
   * The location of the source i18n resource bundle files.
   */
  @Parameter(property="propertiesRootDirectory", defaultValue="src/main/resources/i18n")
  private File propertiesRootDirectory;

  /**
   * Whether to execute the generation of the I18n enum facades.
   * <p>
   * Set to <code>false</code> to skip the generation.
   */
  @Parameter(defaultValue = "true")
  private boolean createJavaFacade;

  private boolean logResolvedPropertyFileNames = true;

  /**
   * The name of the helper class to retrieve translations for the generated enum facade(s).
   */
//  @Parameter(defaultValue="I18n", required=true)
  private String i18nFacadeAccessorName = "I18n";


  /**
   * The package name under which the facade(s) will be generated.
   */
  @Parameter(property="generatedPackage", defaultValue="i18n.generated")
  private String generatedPackage;


  //FIXME: Should be taken from jaxb2 maven plugin
//  @Parameter
  private String[] i18nIncludes;

//  @Parameter
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

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  // custom encoding of generated java files is not supported at the moment since
  // JavaPoet by default always writes as UTF-8
  // It could be possible to manually write via another charset, but at the moment
  // I don't see any reason to do this
//  @Parameter(defaultValue = "${project.build.sourceEncoding}")
  private String javaFileEncoding;

//  @Parameter(defaultValue = "???")
  private String propertyFileEncoding;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws MojoExecutionException {
    if (!this.createJavaFacade) {
      this.getLog()
              .info("Skipping to create the i18n Java facade since it is disabled by the createJavaFacade property within the configuration");
    } else {
      this.getLog().info("Create Java source code facade file from property files.");
      this.logConfigurationProperties();
//      final LocaleFilter localeFilter = this.determineLocaleFilter();
      final Set<File> propertyFileSet = this.resolveFilesFromDirectoryRoot(this.propertiesRootDirectory);

      try {
//        final String i18nFacadeName = StringUtils.defaultString(this.i18nFacadeName,
//                                                                FacadeCreatorHelper.DEFAULT_JAVA_FACADE_FILENAME_I18N_FACADE);

        final FacadeBundleContentHelper fbcHelper = new FacadeBundleContentHelper(propertiesRootDirectory);
        final Map<String, Map<Language, File>> bundleNameToFilesMap = fbcHelper.toBundleNameToFilesMap(propertyFileSet);

        final FacadeCreator facadeCreator = new FacadeCreator();
        for (final Map.Entry<String, Map<Language, File>> entry : bundleNameToFilesMap.entrySet()) {
          final String bundleName = entry.getKey();
          final Map<Language, File> bundleTranslations = entry.getValue();

          final FacadeBundleContent resourceBundleContent = FacadeBundleContent.forName(bundleName).fromFiles(bundleTranslations);
          final TypeSpec resourceBundleEnumTypeSpec = facadeCreator.createFacadeEnumFor(resourceBundleContent);
          final JavaFile javaFile = JavaFile.builder(generatedPackage, resourceBundleEnumTypeSpec).build();
          javaFile.writeTo(outputDirectory);
          // TODO: To allow for custom charsets, we need to call javaFile.toString.getBytes(Charset), but this involves
          //       creating the directoy structure and identifying the correct file name.
        }

        this.project.addCompileSourceRoot(this.outputDirectory.getCanonicalPath());
      } catch (Exception e) {
        this.getLog().error("Could not write Java facade to file", e);
      }

      this.getLog().info("...done");
    }
  }


  /**
   *
   */
  private void logConfigurationProperties() {
    this.getLog().info("createJavaFacade=" + this.createJavaFacade);
    this.getLog().info("javaFileEncoding=" + this.javaFileEncoding);
    this.getLog().info("i18nFacadeName=" + this.i18nFacadeAccessorName);
    this.getLog().info("includeLocaleRegex=" + this.includeLocaleRegex);
    this.getLog().info("excludeLocaleRegex=" + this.excludeLocaleRegex);
    this.getLog().info("i18nIncludes" + Arrays.toString(this.i18nIncludes));
    this.getLog().info("i18nExcludes" + Arrays.toString(this.i18nExcludes));
    this.getLog().info("outputDirectory=" + this.outputDirectory);
    this.getLog().info("generatedPackage=" + this.generatedPackage);
    this.getLog().info("propertiesRootDirectory=" + this.propertiesRootDirectory);
  }




  private Set<File> resolveFilesFromDirectoryRoot(File propertiesRootDirectory) {
    final Set<File> retset = new LinkedHashSet<>();

    final String[] includes = {"**/*.properties"};
    DirectoryScanner directoryScanner = new DirectoryScanner();
    {
      directoryScanner.setIncludes(includes);
      directoryScanner.setBasedir(propertiesRootDirectory);
      directoryScanner.setCaseSensitive(true);
      directoryScanner.scan();
    }

    final String[] fileNames = directoryScanner.getIncludedFiles();
    for (int i = 0; i < fileNames.length; i++) {
      final String fileName = fileNames[i].replaceAll("\\\\", "/");
      if (this.logResolvedPropertyFileNames) {
        this.getLog().info("Resolved: " + fileName);
      }
      retset.add(new File(propertiesRootDirectory, fileName));
    }

    return retset;
  }



  private LocaleFilter determineLocaleFilter() {
    final LocaleFilter localeFilter = new LocaleFilter();
    localeFilter.setPattern(Pattern.compile(this.includeLocaleRegex));
    return localeFilter;
  }
}
