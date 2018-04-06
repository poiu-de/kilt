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
package org.omnaest.i18nbinder.internal.facade.creation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContent.Translation;
import org.omnaest.i18nbinder.internal.facade.I18nBundleKey;


/**
 * This class allow the creation of the I18n Facade(s) for java localization resource
 * bundles.
 *
 * @author mherrn
 */
//TODO: Write unit tests for this class
public class FacadeCreator {
  private static final Logger LOGGER= LogManager.getLogger();


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
   * Creates an enum class for the given resource bundle.
   *
   * @param resourceContent the resource bundle for which to create the enum class.
   * @return the created enum class TypeSpec
   */
  public TypeSpec createFacadeEnumFor(final FacadeBundleContent resourceContent) {
    final String enumName= this.getEnumClassNameFor(resourceContent);

    // build an enum class for the resource bundle
    final TypeSpec.Builder bundleEnumBuilder= TypeSpec.enumBuilder(enumName)
            .addAnnotation(AnnotationSpec.builder(Generated.class)
              .addMember("value", "$S", this.getClass().getName())
              .addMember("date", "$S", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
              .build())
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(I18nBundleKey.class))
            // BASENAME of the bundle as static const
            .addField(FieldSpec.builder(String.class, "BASENAME", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", resourceContent.getBundleName())
                    .build())
            // the resource key as a field of each enum constant
            .addField(String.class, "key", Modifier.PRIVATE, Modifier.FINAL)
            // add a constructor to initialize the resource key
            .addMethod(MethodSpec.constructorBuilder()
                    .addParameter(String.class, "key")
                    .addStatement("this.$N = $N", "key", "key")
                    .build())
            // override getBasename() to return the resource bundle basename
            .addMethod(MethodSpec.methodBuilder("getBasename")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return $N", "BASENAME")
                    .build())
            //.addMethod(MethodSpec.overriding(I18nBundleKey.class.getMethod("getBasename")))
            .addMethod(MethodSpec.methodBuilder("getKey")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return this.$N", "key")
                    .build());

    // now add the actual enum constants
    resourceContent.getContent().asMap().forEach((key, translations) -> {
      bundleEnumBuilder.addEnumConstant(toEnumConstName(key),
                                        TypeSpec.anonymousClassBuilder("$S", key)
                                                .addJavadoc(buildJavadoc(key, translations))
                                                .build());
    });

    // build and return the bundleEnum typeSpec
    return bundleEnumBuilder.build();
  }


  /**
   * Creates a javadoc code block for a single enum constant.
   * It contains the values for each language as an example.
   *
   * @param key the key of the resource
   * @param translations the translations of the key
   * @return
   */
  public CodeBlock buildJavadoc(final String key, final Collection<Translation> translations) {
    final CodeBlock.Builder javadocBuilder= CodeBlock.builder()
            .add("Returns the localized value of <code>$L</code>.\n", key)
            .add("<p>\n")
            .add("Examples:\n")
            .add("<ul>\n");
    for (final Translation translation : translations) {
      javadocBuilder.add("  <li>$L = $L</li>\n", translation.getLang(), translation.getValue());
    }
    javadocBuilder.add("</ul>\n");

    return javadocBuilder.build();
  }


  /**
   * Returns the valid class name for a given resource bundle.
   *
   * @param resourceContent the resource bundle for which to return the class name.
   * @return the class name for this resource bundle
   */
  private String getEnumClassNameFor(final FacadeBundleContent resourceContent) {
    return BundleNormalizer.toClassName(resourceContent.getBundleName());
  }


  /**
   * Converts the given string (usually the key of a resource) to a format
   * suitable as an enum constant.
   *
   * @param s the string to convert
   * @return the converted string
   */
  private String toEnumConstName(final String s) {
    return BundleNormalizer.toConstName(s);
  }


  /**
   * Converts the given string (usually a path to a resource) to a format
   * suitable as a package name.
   *
   * @param s the string to convert
   * @return the converted string
   */
  public String toPackageName(final String s) {
    return s.replaceAll("\\/", ".");
  }
}
