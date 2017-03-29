/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.io;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import com.dua3.meja.util.Option;
import com.dua3.meja.util.Options;

/**
 *
 * @author axel
 */
public abstract class Csv {

  public static final String OPTION_CHARSET = "Character Encoding";
  public static final String OPTION_LOCALE = "Locale";
  public static final String OPTION_DELIMITER = "Text delimiter";
  public static final String OPTION_SEPARATOR = "Separator";

  private static final Options OPTIONS = new Options();

  static {
    OPTIONS.addOption(OPTION_SEPARATOR, Character.class, ',', ';');
    OPTIONS.addOption(OPTION_DELIMITER, Character.class, '"', '\'');
    OPTIONS.addOption(OPTION_LOCALE, Locale.class, Locale.getDefault(), Locale.getAvailableLocales());
    OPTIONS.addOption(OPTION_CHARSET, Charset.class, Charset.defaultCharset(), Charset.availableCharsets().values().toArray(new Charset[0]));
  }

  public static Object getOptionValue(String name, Map<Option<?>, Object> overrides) {
    return OPTIONS.getOptionValue(name, overrides);
  }

}
