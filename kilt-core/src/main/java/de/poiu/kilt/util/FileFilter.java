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
package de.poiu.kilt.util;

import java.util.HashSet;
import java.util.Set;


/**
 * TODO: globFilter + regexFilter?
 *       make immutable?
 *
 * see https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
 *     https://docs.oracle.com/javase/tutorial/essential/io/find.html
 * @author mherrn
 */
public class FileFilter {
  private final Set<String> inclusions= new HashSet<>();
  private final Set<String> exclusions= new HashSet<>();


  public Set<String> getInclusions() {
    return inclusions;
  }


  public Set<String> getExclusions() {
    return exclusions;
  }
}
