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
package de.poiu.kilt.facade.creation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


public class BundleNormalizerTest {

  @Test
  public void testToClassName() {
    final String input = "Tĥïŝ-ĩš â__fůňķŷ_Šťŕĭńġ.äÄüÿÆ¼.!/\\?+*]{[}$#@@!$%^&&ß!$%&'()*+,-./:;<=>?@[\\]^_`{|}~\"";
    final String expected= "ThisIsAFunkyStringAeAEueyAE14Ss";

    final String actual= BundleNormalizer.toClassName(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToClassName_startsWithNumber() {
    final String input = "1st of a series";
    final String expected= "_1stOfASeries";

    final String actual= BundleNormalizer.toClassName(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToFieldName() {
    final String input = "Tĥïŝ-ĩš â__fůňķŷ_Šťŕĭńġ.äÄüÿÆ¼.!/\\?+*]{[}$#@@!$%^&&ß!$%&'()*+,-./:;<=>?@[\\]^_`{|}~\"";
    final String expected= "thisIsAFunkyStringAeAEueyAE14Ss";

    final String actual= BundleNormalizer.toFieldName(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToFieldName_startsWithNumber() {
    final String input = "1st of a series";
    final String expected= "_1stOfASeries";

    final String actual= BundleNormalizer.toFieldName(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToConstName() {
    final String input = "Tĥïŝ-ĩš â__fůňķŷ_Šťŕĭńġ.äÄüÿÆ¼.!/\\?+*]{[}$#@@!$%^&&ß!$%&'()*+,-./:;<=>?@[\\]^_`{|}~\"";
    final String expected= "THIS_IS_A_FUNKY_STRING_AEAEUEYAE14_SS";

    final String actual= BundleNormalizer.toConstName(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToConstName_startsWithNumber() {
    final String input = "1st of a series";
    final String expected= "_1ST_OF_A_SERIES";

    final String actual= BundleNormalizer.toConstName(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNormalize() {
    final String input = "Tĥïŝ-ĩš â__fůňķŷ_Šťŕĭńġ.äÄüÿÆ¼.!/\\?+*]{[}$#@@!$%^&&ß!$%&'()*+,-./:;<=>?@[\\]^_`{|}~\"";
    final String expected= "This_is_a_funky_String_aeAEueyAE14_ss";

    final String actual= BundleNormalizer.normalize(input);

    assertThat(actual).isEqualTo(expected);
  }


  @Test
  public void testNormalize_alreadyNormalized() {
    final String input = "This_is_a_funky_String_aeAEueyAE14_ss";
    final String expected= "This_is_a_funky_String_aeAEueyAE14_ss";

    final String actual= BundleNormalizer.normalize(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNormalize_startsWithNumber() {
    final String input = "1stOfaSeries";
    final String expected= "1stOfaSeries";

    final String actual= BundleNormalizer.normalize(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNormalize_withLineBreak() {
    final String input = "text\nover\nmultiple\nlines";
    final String expected= "text_over_multiple_lines";

    final String actual= BundleNormalizer.normalize(input);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToBundleName() {
    final String input = "my/bundles/path";
    final String expected= "my.bundles.path";

    final String actual= BundleNormalizer.toBundleName(input);

    assertThat(actual).isEqualTo(expected);
  }

}
