/*
 * Copyright 2016 a5xysq1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Color in ARGB format.
 */
@SuppressWarnings("serial")
public final class Color
        implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Map<String, Color> COLORS = new LinkedHashMap<>();

    // predefined Color constants
    public static final Color BLACK = register("BLACK", 0xFF000000);
    public static final Color SILVER = register("SILVER", 0xFFC0C0C0);
    public static final Color GRAY = register("GRAY", 0xFF808080);
    public static final Color WHITE = register("WHITE", 0xFFFFFFFF);
    public static final Color MAROON = register("MAROON", 0xFF800000);
    public static final Color RED = register("RED", 0xFFFF0000);
    public static final Color PURPLE = register("PURPLE", 0xFF800080);
    public static final Color FUCHSIA = register("FUCHSIA", 0xFFFF00FF);
    public static final Color GREEN = register("GREEN", 0xFF008000);
    public static final Color LIME = register("LIME", 0xFF00FF00);
    public static final Color OLIVE = register("OLIVE", 0xFF808000);
    public static final Color YELLOW = register("YELLOW", 0xFFFFFF00);
    public static final Color NAVY = register("NAVY", 0xFF000080);
    public static final Color BLUE = register("BLUE", 0xFF0000FF);
    public static final Color TEAL = register("TEAL", 0xFF008080);
    public static final Color AQUA = register("AQUA", 0xFF00FFFF);
    public static final Color ORANGE = register("ORANGE", 0xFFFFA500);
    public static final Color ALICEBLUE = register("ALICEBLUE", 0xFFF0F8FF);
    public static final Color ANTIQUEWHITE = register("ANTIQUEWHITE", 0xFFFAEBD7);
    public static final Color AQUAMARINE = register("AQUAMARINE", 0xFF7FFFD4);
    public static final Color AZURE = register("AZURE", 0xFFF0FFFF);
    public static final Color BEIGE = register("BEIGE", 0xFFF5F5DC);
    public static final Color BISQUE = register("BISQUE", 0xFFFFE4C4);
    public static final Color BLANCHEDALMOND = register("BLANCHEDALMOND", 0xFFFFE4C4);
    public static final Color BLUEVIOLET = register("BLUEVIOLET", 0xFF8A2BE2);
    public static final Color BROWN = register("BROWN", 0xFFA52A2A);
    public static final Color BURLYWOOD = register("BURLYWOOD", 0xFFDEB887);
    public static final Color CADETBLUE = register("CADETBLUE", 0xFF5F9EA0);
    public static final Color CHARTREUSE = register("CHARTREUSE", 0xFF7FFF00);
    public static final Color CHOCOLATE = register("CHOCOLATE", 0xFFD2691E);
    public static final Color CORAL = register("CORAL", 0xFFFF7F50);
    public static final Color CORNFLOWERBLUE = register("CORNFLOWERBLUE", 0xFF6495ED);
    public static final Color CORNSILK = register("CORNSILK", 0xFFFFF8DC);
    public static final Color CRIMSON = register("CRIMSON", 0xFFDC143C);
    public static final Color DARKBLUE = register("DARKBLUE", 0xFF00008B);
    public static final Color DARKCYAN = register("DARKCYAN", 0xFF008B8B);
    public static final Color DARKGOLDENROD = register("DARKGOLDENROD", 0xFFB8860B);
    public static final Color DARKGRAY = register("DARKGRAY", 0xFFA9A9A9);
    public static final Color DARKGREEN = register("DARKGREEN", 0xFF006400);
    public static final Color DARKGREY = register("DARKGREY", 0xFFA9A9A9);
    public static final Color DARKKHAKI = register("DARKKHAKI", 0xFFBDB76B);
    public static final Color DARKMAGENTA = register("DARKMAGENTA", 0xFF8B008B);
    public static final Color DARKOLIVEGREEN = register("DARKOLIVEGREEN", 0xFF556B2F);
    public static final Color DARKORANGE = register("DARKORANGE", 0xFFFF8C00);
    public static final Color DARKORCHID = register("DARKORCHID", 0xFF9932CC);
    public static final Color DARKRED = register("DARKRED", 0xFF8B0000);
    public static final Color DARKSALMON = register("DARKSALMON", 0xFFE9967A);
    public static final Color DARKSEAGREEN = register("DARKSEAGREEN", 0xFF8FBC8F);
    public static final Color DARKSLATEBLUE = register("DARKSLATEBLUE", 0xFF483D8B);
    public static final Color DARKSLATEGRAY = register("DARKSLATEGRAY", 0xFF2F4F4F);
    public static final Color DARKSLATEGREY = register("DARKSLATEGREY", 0xFF2F4F4F);
    public static final Color DARKTURQUOISE = register("DARKTURQUOISE", 0xFF00CED1);
    public static final Color DARKVIOLET = register("DARKVIOLET", 0xFF9400D3);
    public static final Color DEEPPINK = register("DEEPPINK", 0xFFFF1493);
    public static final Color DEEPSKYBLUE = register("DEEPSKYBLUE", 0xFF00BFFF);
    public static final Color DIMGRAY = register("DIMGRAY", 0xFF696969);
    public static final Color DIMGREY = register("DIMGREY", 0xFF696969);
    public static final Color DODGERBLUE = register("DODGERBLUE", 0xFF1E90FF);
    public static final Color FIREBRICK = register("FIREBRICK", 0xFFB22222);
    public static final Color FLORALWHITE = register("FLORALWHITE", 0xFFFFFAF0);
    public static final Color FORESTGREEN = register("FORESTGREEN", 0xFF228B22);
    public static final Color GAINSBORO = register("GAINSBORO", 0xFFDCDCDC);
    public static final Color GHOSTWHITE = register("GHOSTWHITE", 0xFFF8F8FF);
    public static final Color GOLD = register("GOLD", 0xFFFFD700);
    public static final Color GOLDENROD = register("GOLDENROD", 0xFFDAA520);
    public static final Color GREENYELLOW = register("GREENYELLOW", 0xFFADFF2F);
    public static final Color GREY = register("GREY", 0xFF808080);
    public static final Color HONEYDEW = register("HONEYDEW", 0xFFF0FFF0);
    public static final Color HOTPINK = register("HOTPINK", 0xFFFF69B4);
    public static final Color INDIANRED = register("INDIANRED", 0xFFCD5C5C);
    public static final Color INDIGO = register("INDIGO", 0xFF4B0082);
    public static final Color IVORY = register("IVORY", 0xFFFFFFF0);
    public static final Color KHAKI = register("KHAKI", 0xFFF0E68C);
    public static final Color LAVENDER = register("LAVENDER", 0xFFE6E6FA);
    public static final Color LAVENDERBLUSH = register("LAVENDERBLUSH", 0xFFFFF0F5);
    public static final Color LAWNGREEN = register("LAWNGREEN", 0xFF7CFC00);
    public static final Color LEMONCHIFFON = register("LEMONCHIFFON", 0xFFFFFACD);
    public static final Color LIGHTBLUE = register("LIGHTBLUE", 0xFFADD8E6);
    public static final Color LIGHTCORAL = register("LIGHTCORAL", 0xFFF08080);
    public static final Color LIGHTCYAN = register("LIGHTCYAN", 0xFFE0FFFF);
    public static final Color LIGHTGOLDENRODYELLOW = register("LIGHTGOLDENRODYELLOW", 0xFFFAFAD2);
    public static final Color LIGHTGRAY = register("LIGHTGRAY", 0xFFD3D3D3);
    public static final Color LIGHTGREEN = register("LIGHTGREEN", 0xFF90EE90);
    public static final Color LIGHTGREY = register("LIGHTGREY", 0xFFD3D3D3);
    public static final Color LIGHTPINK = register("LIGHTPINK", 0xFFFFB6C1);
    public static final Color LIGHTSALMON = register("LIGHTSALMON", 0xFFFFA07A);
    public static final Color LIGHTSEAGREEN = register("LIGHTSEAGREEN", 0xFF20B2AA);
    public static final Color LIGHTSKYBLUE = register("LIGHTSKYBLUE", 0xFF87CEFA);
    public static final Color LIGHTSLATEGRAY = register("LIGHTSLATEGRAY", 0xFF778899);
    public static final Color LIGHTSLATEGREY = register("LIGHTSLATEGREY", 0xFF778899);
    public static final Color LIGHTSTEELBLUE = register("LIGHTSTEELBLUE", 0xFFB0C4DE);
    public static final Color LIGHTYELLOW = register("LIGHTYELLOW", 0xFFFFFFE0);
    public static final Color LIMEGREEN = register("LIMEGREEN", 0xFF32CD32);
    public static final Color LINEN = register("LINEN", 0xFFFAF0E6);
    public static final Color MEDIUMAQUAMARINE = register("MEDIUMAQUAMARINE", 0xFF66CDAA);
    public static final Color MEDIUMBLUE = register("MEDIUMBLUE", 0xFF0000CD);
    public static final Color MEDIUMORCHID = register("MEDIUMORCHID", 0xFFBA55D3);
    public static final Color MEDIUMPURPLE = register("MEDIUMPURPLE", 0xFF9370DB);
    public static final Color MEDIUMSEAGREEN = register("MEDIUMSEAGREEN", 0xFF3CB371);
    public static final Color MEDIUMSLATEBLUE = register("MEDIUMSLATEBLUE", 0xFF7B68EE);
    public static final Color MEDIUMSPRINGGREEN = register("MEDIUMSPRINGGREEN", 0xFF00FA9A);
    public static final Color MEDIUMTURQUOISE = register("MEDIUMTURQUOISE", 0xFF48D1CC);
    public static final Color MEDIUMVIOLETRED = register("MEDIUMVIOLETRED", 0xFFC71585);
    public static final Color MIDNIGHTBLUE = register("MIDNIGHTBLUE", 0xFF191970);
    public static final Color MINTCREAM = register("MINTCREAM", 0xFFF5FFFA);
    public static final Color MISTYROSE = register("MISTYROSE", 0xFFFFE4E1);
    public static final Color MOCCASIN = register("MOCCASIN", 0xFFFFE4B5);
    public static final Color NAVAJOWHITE = register("NAVAJOWHITE", 0xFFFFDEAD);
    public static final Color OLDLACE = register("OLDLACE", 0xFFFDF5E6);
    public static final Color OLIVEDRAB = register("OLIVEDRAB", 0xFF6B8E23);
    public static final Color ORANGERED = register("ORANGERED", 0xFFFF4500);
    public static final Color ORCHID = register("ORCHID", 0xFFDA70D6);
    public static final Color PALEGOLDENROD = register("PALEGOLDENROD", 0xFFEEE8AA);
    public static final Color PALEGREEN = register("PALEGREEN", 0xFF98FB98);
    public static final Color PALETURQUOISE = register("PALETURQUOISE", 0xFFAFEEEE);
    public static final Color PALEVIOLETRED = register("PALEVIOLETRED", 0xFFDB7093);
    public static final Color PAPAYAWHIP = register("PAPAYAWHIP", 0xFFFFEFD5);
    public static final Color PEACHPUFF = register("PEACHPUFF", 0xFFFFDAB9);
    public static final Color PERU = register("PERU", 0xFFCD853F);
    public static final Color PINK = register("PINK", 0xFFFFC0CB);
    public static final Color PLUM = register("PLUM", 0xFFDDA0DD);
    public static final Color POWDERBLUE = register("POWDERBLUE", 0xFFB0E0E6);
    public static final Color ROSYBROWN = register("ROSYBROWN", 0xFFBC8F8F);
    public static final Color ROYALBLUE = register("ROYALBLUE", 0xFF4169E1);
    public static final Color SADDLEBROWN = register("SADDLEBROWN", 0xFF8B4513);
    public static final Color SALMON = register("SALMON", 0xFFFA8072);
    public static final Color SANDYBROWN = register("SANDYBROWN", 0xFFF4A460);
    public static final Color SEAGREEN = register("SEAGREEN", 0xFF2E8B57);
    public static final Color SEASHELL = register("SEASHELL", 0xFFFFF5EE);
    public static final Color SIENNA = register("SIENNA", 0xFFA0522D);
    public static final Color SKYBLUE = register("SKYBLUE", 0xFF87CEEB);
    public static final Color SLATEBLUE = register("SLATEBLUE", 0xFF6A5ACD);
    public static final Color SLATEGRAY = register("SLATEGRAY", 0xFF708090);
    public static final Color SLATEGREY = register("SLATEGREY", 0xFF708090);
    public static final Color SNOW = register("SNOW", 0xFFFFFAFA);
    public static final Color SPRINGGREEN = register("SPRINGGREEN", 0xFF00FF7F);
    public static final Color STEELBLUE = register("STEELBLUE", 0xFF4682B4);
    public static final Color TAN = register("TAN", 0xFFD2B48C);
    public static final Color THISTLE = register("THISTLE", 0xFFD8BFD8);
    public static final Color TOMATO = register("TOMATO", 0xFFFF6347);
    public static final Color TURQUOISE = register("TURQUOISE", 0xFF40E0D0);
    public static final Color VIOLET = register("VIOLET", 0xFFEE82EE);
    public static final Color WHEAT = register("WHEAT", 0xFFF5DEB3);
    public static final Color WHITESMOKE = register("WHITESMOKE", 0xFFF5F5F5);
    public static final Color YELLOWGREEN = register("YELLOWGREEN", 0xFF9ACD32);
    public static final Color REBECCAPURPLE = register("REBECCAPURPLE", 0xFF663399);

    private static final int SHIFT_A = 24;

    private static final int SHIFT_R = 16;

    private static final int SHIFT_G = 8;

    private static final int SHIFT_B = 0;

    private static final double F_BRIGHTEN = 0.7;

    public static Map<String, Color> palette() {
        return Collections.unmodifiableMap(COLORS);
    }

    private static Color register(String name, int code) {
        Color c = new Color(code);
        COLORS.put(name, c);
        return c;
    }

    private static int shiftComponentValue(int value, int bits) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException();
        }
        return value << bits;
    }

    public static Color valueOf(String s) {
        // try named colors first
        Color color = COLORS.get(s);
        if (color != null) {
            return color;
        }

        // HEX colors
        if (s.startsWith("#")) {
            int i = Integer.parseUnsignedInt(s.substring(1), 16);
            return new Color(i);
        }

        // RGB colors. example: "rgb(255, 0, 0)"
        if (s.startsWith("rgb")) {
            String s1 = s.substring(3).trim();
            if (s1.charAt(0) == '(' && s1.charAt(s.length() - 1) == ')') {
                String[] parts = s1.split(",");
                if (parts.length == 3) {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    return new Color(r, g, b);
                }
            }
            throw new IllegalArgumentException("Cannot parse \"" + s + "\" as rgb color.");
        }

        // RGBA colors. example: "rgb(255, 0, 0, 0.3)"
        if (s.startsWith("rgba")) {
            String s1 = s.substring(4).trim();
            if (s1.charAt(0) == '(' && s1.charAt(s.length() - 1) == ')') {
                String[] parts = s1.split(",");
                if (parts.length == 4) {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    int a = Math.round(255 * Float.parseFloat(parts[3]));
                    return new Color(r, g, b, a);
                }
            }
            throw new IllegalArgumentException("Cannot parse \"" + s + "\" as rgba color.");
        }

        // no luck so far
        throw new IllegalArgumentException("\"" + s + "\" is no valid color.");
    }

    public static Iterable<Color> values() {
        return COLORS.values();
    }

    private final int argb;

    private Color(int argb) {
        this.argb = argb;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        argb = shiftComponentValue(a, SHIFT_A)
                + shiftComponentValue(r, SHIFT_R)
                + shiftComponentValue(g, SHIFT_G)
                + shiftComponentValue(b, SHIFT_B);
    }

    public int a() {
        return (argb >> SHIFT_A) & 0xff;
    }

    public float af() {
        return a() / 255f;
    }

    public int argb() {
        return argb;
    }

    public int b() {
        return (argb >> SHIFT_B) & 0xff;
    }

    public float bf() {
        return b() / 255f;
    }

    public Color brighter() {
        int r = r();
        int g = g();
        int b = b();
        int alpha = a();

        int i = (int) (1.0 / (1.0 - F_BRIGHTEN));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }

        if (r > 0 && r < i) {
            r = i;
        }
        if (g > 0 && g < i) {
            g = i;
        }
        if (b > 0 && b < i) {
            b = i;
        }

        return new Color(
                Math.min((int) (r / F_BRIGHTEN), 255),
                Math.min((int) (g / F_BRIGHTEN), 255),
                Math.min((int) (b / F_BRIGHTEN), 255),
                alpha);
    }

    public Color darker() {
        return new Color(
                Math.max((int) (r() * F_BRIGHTEN), 0),
                Math.max((int) (g() * F_BRIGHTEN), 0),
                Math.max((int) (b() * F_BRIGHTEN), 0),
                a());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        return ((Color) obj).argb == argb;
    }

    public int g() {
        return (argb >> SHIFT_G) & 0xff;
    }

    public float gf() {
        return g() / 255f;
    }

    @Override
    public int hashCode() {
        return argb;
    }

    public int r() {
        return (argb >> SHIFT_R) & 0xff;
    }

    public float rf() {
        return r() / 255f;
    }

    public byte[] toByteArray() {
        byte[] arr = {
                (byte) a(), (byte) r(), (byte) g(), (byte) b()
        };
        return arr;
    }

    @Override
    public String toString() {
        return "#" + Integer.toHexString(argb);
    }

}
