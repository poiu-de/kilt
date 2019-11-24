/*
 * Copyright (C) 2019 Marco Herrn
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

import de.poiu.kilt.bundlecontent.Language;
import de.poiu.kilt.bundlecontent.Translation;
import java.util.Comparator;


/**
 *
 * @author mherrn
 */
public class TranslationComparator implements Comparator<Translation> {

  public static final TranslationComparator INSTANCE= new TranslationComparator();

  @Override
  public int compare(final Translation o1, final Translation o2) {
    if (o1 == o2) {
      return 0;
    }

    if (o1 == null) {
      return 1;
    }

    if (o2 == null) {
      return -1;
    }

    if (o1.getLang().equals(o2.getLang())) {
      return o1.getValue().compareTo(o2.getValue());
    }

    if (o1.getLang().equals(Language.of(""))) {
      return -1;
    } else if (o2.getLang().equals(Language.of(""))) {
      return 1;
    }

    if (o1.getLang().equals(Language.of("_"))) {
      return -1;
    } else if (o2.getLang().equals(Language.of("_"))) {
      return 1;
    }

    if (o1.getLang().getLang().startsWith("<")) {
      return -1;
    } else if (o2.getLang().getLang().startsWith("<")) {
      return 1;
    }

    return o1.getLang().compareTo(o2.getLang());
  }


}
