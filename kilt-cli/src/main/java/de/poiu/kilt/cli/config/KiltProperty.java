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
package de.poiu.kilt.cli.config;


/**
 *
 * @author mherrn
 */
public enum KiltProperty {
  PROPERTIES_ROOT_DIRECTORY("propertiesRootDirectory"),
  I18N_INCLUDES("i18nIncludes"),
  I18N_EXCLUDES("i18nExcludes"),
  PROPERTY_FILE_ENCODING("propertyFileEncoding"),
  XLS_FILE("xlsFile"),
  DELETE_EMPTY_PROPERTIES("deleteEmptyProperties"),
  MISSING_KEY_ACTION("missingKeyAction"),
  FACADE_GENERATION_DIR("facadeGenerationDir"),
  GENERATED_PACKAGE("generatedPackage"),
  COPY_FACADE_ACCESSOR_CLASSES("copyFacadeAccessorClasses"),
  FACADE_ACCESSOR_CLASS_NAME("facadeAccessorClassName"),
  VERBOSE("verbose"),
  ;


  private final String key;

  private KiltProperty(final String key) {
    this.key= key;
  }


  public String getKey() {
    return key;
  }


}
