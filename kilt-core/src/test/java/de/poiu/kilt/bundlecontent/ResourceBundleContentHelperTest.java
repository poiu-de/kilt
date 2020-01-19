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

import de.poiu.kilt.bundlecontent.ResourceBundleContentHelper;
import de.poiu.kilt.bundlecontent.Language;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


/**
 *
 * @author mherrn
 */
public class ResourceBundleContentHelperTest {

  @Test
  public void test_ResourceBundleRegex_onlyBasename() {
    // preparation
    final String resourceBundleFileName= "myBasename.properties";

    // execution
    final Matcher matcher= ResourceBundleContentHelper.PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(resourceBundleFileName);

    // verification
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("BUNDLE")).isEqualTo("myBasename");
    assertThat(matcher.group("LANG")).isNull();
    assertThat(matcher.group("SCRIPT")).isNull();
    assertThat(matcher.group("COUNTRY")).isNull();
    assertThat(matcher.group("VARIANT")).isNull();
    assertThat(matcher.group("LOCALE")).isNull();
  }

  @Test
  public void test_ResourceBundleRegex_withLang() {
    // preparation
    final String resourceBundleFileName= "myBasename_de.properties";

    // execution
    final Matcher matcher= ResourceBundleContentHelper.PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(resourceBundleFileName);

    // verification
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("BUNDLE")).isEqualTo("myBasename");
    assertThat(matcher.group("LANG")).isEqualTo("de");
    assertThat(matcher.group("SCRIPT")).isNull();
    assertThat(matcher.group("COUNTRY")).isNull();
    assertThat(matcher.group("VARIANT")).isNull();
    assertThat(matcher.group("LOCALE")).isEqualTo("de");
  }

  @Test
  public void test_ResourceBundleRegex_withAlpha3Lang() {
    // preparation
    final String resourceBundleFileName= "myBasename_deu.properties";

    // execution
    final Matcher matcher= ResourceBundleContentHelper.PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(resourceBundleFileName);

    // verification
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("BUNDLE")).isEqualTo("myBasename");
    assertThat(matcher.group("LANG")).isEqualTo("deu");
    assertThat(matcher.group("SCRIPT")).isNull();
    assertThat(matcher.group("COUNTRY")).isNull();
    assertThat(matcher.group("VARIANT")).isNull();
    assertThat(matcher.group("LOCALE")).isEqualTo("deu");
  }

  @Test
  public void test_ResourceBundleRegex_withLangAndCountry() {
    // preparation
    final String resourceBundleFileName= "myBasename_en_US.properties";

    // execution
    final Matcher matcher= ResourceBundleContentHelper.PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(resourceBundleFileName);

    // verification
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("BUNDLE")).isEqualTo("myBasename");
    assertThat(matcher.group("LANG")).isEqualTo("en");
    assertThat(matcher.group("SCRIPT")).isNull();
    assertThat(matcher.group("COUNTRY")).isEqualTo("US");
    assertThat(matcher.group("VARIANT")).isNull();
    assertThat(matcher.group("LOCALE")).isEqualTo("en_US");
  }

  @Test
  public void test_ResourceBundleRegex_withLangAndScript() {
    // preparation
    final String resourceBundleFileName= "myBasename_en_Latn.properties";

    // execution
    final Matcher matcher= ResourceBundleContentHelper.PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(resourceBundleFileName);

    // verification
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("BUNDLE")).isEqualTo("myBasename");
    assertThat(matcher.group("LANG")).isEqualTo("en");
    assertThat(matcher.group("SCRIPT")).isEqualTo("Latn");
    assertThat(matcher.group("COUNTRY")).isNull();
    assertThat(matcher.group("VARIANT")).isNull();
    assertThat(matcher.group("LOCALE")).isEqualTo("en_Latn");
  }

  @Test
  public void test_ResourceBundleRegex_withLangAndScriptAndCountry() {
    // preparation
    final String resourceBundleFileName= "myBasename_en_Latn_US.properties";

    // execution
    final Matcher matcher= ResourceBundleContentHelper.PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(resourceBundleFileName);

    // verification
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("BUNDLE")).isEqualTo("myBasename");
    assertThat(matcher.group("LANG")).isEqualTo("en");
    assertThat(matcher.group("SCRIPT")).isEqualTo("Latn");
    assertThat(matcher.group("COUNTRY")).isEqualTo("US");
    assertThat(matcher.group("VARIANT")).isNull();
    assertThat(matcher.group("LOCALE")).isEqualTo("en_Latn_US");
  }

  @Test
  public void test_ResourceBundleRegex_withLangAndCountryAndVariant() {
    // preparation
    final String resourceBundleFileName= "myBasename_en_US_WINDOWSVISTA.properties";

    // execution
    final Matcher matcher= ResourceBundleContentHelper.PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(resourceBundleFileName);

    // verification
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("BUNDLE")).isEqualTo("myBasename");
    assertThat(matcher.group("LANG")).isEqualTo("en");
    assertThat(matcher.group("SCRIPT")).isNull();
    assertThat(matcher.group("COUNTRY")).isEqualTo("US");
    assertThat(matcher.group("VARIANT")).isEqualTo("WINDOWSVISTA");
    assertThat(matcher.group("LOCALE")).isEqualTo("en_US_WINDOWSVISTA");
  }

  @Test
  public void test_ResourceBundleRegex_withLangAndScriptAndCountryAndVariant() {
    // preparation
    final String resourceBundleFileName= "myBasename_en_Latn_US_WINDOWSVISTA.properties";

    // execution
    final Matcher matcher= ResourceBundleContentHelper.PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(resourceBundleFileName);

    // verification
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("BUNDLE")).isEqualTo("myBasename");
    assertThat(matcher.group("LANG")).isEqualTo("en");
    assertThat(matcher.group("SCRIPT")).isEqualTo("Latn");
    assertThat(matcher.group("COUNTRY")).isEqualTo("US");
    assertThat(matcher.group("VARIANT")).isEqualTo("WINDOWSVISTA");
    assertThat(matcher.group("LOCALE")).isEqualTo("en_Latn_US_WINDOWSVISTA");
  }


  @Test
  public void testGetBundlePrefix() {
    final Path ignorableBasePath= Paths.get("/some/path/src/main/resources");
    final ResourceBundleContentHelper helper= new ResourceBundleContentHelper(ignorableBasePath);
    assertThat(helper.getBundlePrefix(ignorableBasePath.resolve("i18n/messages_de.properties"))).isEqualTo("i18n");
    assertThat(helper.getBundlePrefix(ignorableBasePath.resolve("i18n/my/sub/options_de.properties"))).isEqualTo("i18n/my/sub");
  }


  @Test
  public void testGetBundlePrefix_inWrongBasePath() {
    final Path ignorableBasePath= Paths.get("/some/path/src/main/resources");
    final Path otherBasePath= Paths.get("/some/other/path");
    final ResourceBundleContentHelper helper= new ResourceBundleContentHelper(ignorableBasePath);
    assertThatIllegalArgumentException().isThrownBy(() -> {
      helper.getBundlePrefix(otherBasePath.resolve("i18n/messages_de.properties"));
    }).withMessage("All files should live below the ignorable base path /some/path/src/main/resources. Given path is /some/other/path/i18n/messages_de.properties");
  }


  @Test
  public void testGetBundlePrefix_byPathOrFile() {
    final Path ignorableBasePath= Paths.get("/some/path/src/main/resources");
    final ResourceBundleContentHelper helper= new ResourceBundleContentHelper(ignorableBasePath);
    assertThat(helper.getBundlePrefix(ignorableBasePath.resolve("i18n/messages_de.properties"))).isEqualTo("i18n");
    assertThat(helper.getBundlePrefix(ignorableBasePath.resolve("i18n/messages_de.properties").toFile())).isEqualTo("i18n");
    assertThat(helper.getBundlePrefix(new File(ignorableBasePath.toFile(), "i18n/messages_de.properties"))).isEqualTo("i18n");
  }


  @Test
  public void testToBundleNameToFilesMap() {
    // preparation
    final Path ignorableBasePath= Paths.get("/some/path/src/main/resources");

    final List<File> resourceFiles= ImmutableList.of(
            ignorableBasePath.resolve("i18n/messages_de.properties").toFile(),
            ignorableBasePath.resolve("i18n/messages_en.properties").toFile(),
            ignorableBasePath.resolve("i18n/options.properties").toFile(),
            ignorableBasePath.resolve("i18n/options_en_US.properties").toFile(),
            ignorableBasePath.resolve("i18n/options_en_GB.properties").toFile()
    );

    // execution
    final ResourceBundleContentHelper helper= new ResourceBundleContentHelper(ignorableBasePath);
    final Map<String, Map<Language, File>> bundleNameToFilesMap = helper.toBundleNameToFilesMap(resourceFiles);

    System.out.println(bundleNameToFilesMap.keySet());
    // verification
    assertThat(bundleNameToFilesMap.keySet()).containsExactly("i18n/messages", "i18n/options");
    assertThat(bundleNameToFilesMap.get("i18n/messages")).containsKeys(Language.of("de"), Language.of("en"));
    assertThat(bundleNameToFilesMap.get("i18n/messages").get(Language.of("de"))).hasName("messages_de.properties");
    assertThat(bundleNameToFilesMap.get("i18n/messages").get(Language.of("en"))).hasName("messages_en.properties");
    assertThat(bundleNameToFilesMap.get("i18n/options")).containsKeys(Language.of(""), Language.of("en_US"), Language.of("en_GB"));
    assertThat(bundleNameToFilesMap.get("i18n/options").get(Language.of(""))).hasName("options.properties");
    assertThat(bundleNameToFilesMap.get("i18n/options").get(Language.of("en_US"))).hasName("options_en_US.properties");
    assertThat(bundleNameToFilesMap.get("i18n/options").get(Language.of("en_GB"))).hasName("options_en_GB.properties");
  }


  @Test
  public void testToBundleNameToFilesMap_IgnoreSubDir() {
    // preparation
    final Path ignorableBasePath= Paths.get("/some/path/src/main/resources");

    final List<File> resourceFiles= ImmutableList.of(
            ignorableBasePath.resolve("messages_de.properties").toFile(),
            ignorableBasePath.resolve("messages_en.properties").toFile(),
            ignorableBasePath.resolve("options.properties").toFile(),
            ignorableBasePath.resolve("options_en_US.properties").toFile(),
            ignorableBasePath.resolve("options_en_GB.properties").toFile()
    );

    // execution
    final ResourceBundleContentHelper helper= new ResourceBundleContentHelper(ignorableBasePath);
    final Map<String, Map<Language, File>> bundleNameToFilesMap = helper.toBundleNameToFilesMap(resourceFiles);

    System.out.println(bundleNameToFilesMap.keySet());
    // verification
    assertThat(bundleNameToFilesMap.keySet()).containsExactly("messages", "options");
    assertThat(bundleNameToFilesMap.get("messages")).containsKeys(Language.of("de"), Language.of("en"));
    assertThat(bundleNameToFilesMap.get("messages").get(Language.of("de"))).hasName("messages_de.properties");
    assertThat(bundleNameToFilesMap.get("messages").get(Language.of("en"))).hasName("messages_en.properties");
    assertThat(bundleNameToFilesMap.get("options")).containsKeys(Language.of(""), Language.of("en_US"), Language.of("en_GB"));
    assertThat(bundleNameToFilesMap.get("options").get(Language.of(""))).hasName("options.properties");
    assertThat(bundleNameToFilesMap.get("options").get(Language.of("en_US"))).hasName("options_en_US.properties");
    assertThat(bundleNameToFilesMap.get("options").get(Language.of("en_GB"))).hasName("options_en_GB.properties");
  }
}
