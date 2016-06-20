/*
 * Copyright 2016 Axel Howind <axel@dua3.com>.
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
package com.dua3.meja.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Axel Howind
 */
public class Style {

    // font properties
    public static final String FONT_FAMILY = "font-family";
    public static final String FONT_STYLE = "font-style";
    public static final String FONT_SIZE = "font-size";
    public static final String FONT_WEIGHT = "font-weight";
    public static final String FONT_VARIANT = "font-variant";

    public static final String TEXT_DECORATION = "text-decoration";

    // colors
    public static final String COLOR = "color";
    public static final String BACKGROUND_COLOR = "background-color";

    private static final Style NONE = new Style();

    static Style none() {
        return NONE;
    }

    private final Map<String, String> properties = new HashMap<>();

    void put(String property, String value) {
        properties.put(property, value);
    }

    String get(String property) {
        return properties.get(property);
    }

    void remove(String property) {
        properties.remove(property);
    }

    String getOrDefault(String property, String def) {
        return properties.getOrDefault(property, def);
    }

    public Map<String,String> properties() {
        return Collections.unmodifiableMap(properties);
    }

}
