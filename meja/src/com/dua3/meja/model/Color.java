/*
 * Copyright 2016 a5xysq1.
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
package com.dua3.meja.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Color in ARGB format.
 */
public class Color {

    private static final Map<String, Color> COLORS = new HashMap<>();

    // predefined Color constants
    public static final Color BLACK = register("BLACK", 0X000000);
    public static final Color SILVER = register("SILVER", 0XC0C0C0);
    public static final Color GRAY = register("GRAY", 0X808080);
    public static final Color WHITE = register("WHITE", 0XFFFFFF);
    public static final Color MAROON = register("MAROON", 0X800000);
    public static final Color RED = register("RED", 0XFF0000);
    public static final Color PURPLE = register("PURPLE", 0X800080);
    public static final Color FUCHSIA = register("FUCHSIA", 0XFF00FF);
    public static final Color GREEN = register("GREEN", 0X008000);
    public static final Color LIME = register("LIME", 0X00FF00);
    public static final Color OLIVE = register("OLIVE", 0X808000);
    public static final Color YELLOW = register("YELLOW", 0XFFFF00);
    public static final Color NAVY = register("NAVY", 0X000080);
    public static final Color BLUE = register("BLUE", 0X0000FF);
    public static final Color TEAL = register("TEAL", 0X008080);
    public static final Color AQUA = register("AQUA", 0X00FFFF);
    public static final Color ORANGE = register("ORANGE", 0XFFA500);
    public static final Color ALICEBLUE = register("ALICEBLUE", 0XF0F8FF);
    public static final Color ANTIQUEWHITE = register("ANTIQUEWHITE", 0XFAEBD7);
    public static final Color AQUAMARINE = register("AQUAMARINE", 0X7FFFD4);
    public static final Color AZURE = register("AZURE", 0XF0FFFF);
    public static final Color BEIGE = register("BEIGE", 0XF5F5DC);
    public static final Color BISQUE = register("BISQUE", 0XFFE4C4);
    public static final Color BLANCHEDALMOND = register("BLANCHEDALMOND", 0XFFE4C4);
    public static final Color BLUEVIOLET = register("BLUEVIOLET", 0X8A2BE2);
    public static final Color BROWN = register("BROWN", 0XA52A2A);
    public static final Color BURLYWOOD = register("BURLYWOOD", 0XDEB887);
    public static final Color CADETBLUE = register("CADETBLUE", 0X5F9EA0);
    public static final Color CHARTREUSE = register("CHARTREUSE", 0X7FFF00);
    public static final Color CHOCOLATE = register("CHOCOLATE", 0XD2691E);
    public static final Color CORAL = register("CORAL", 0XFF7F50);
    public static final Color CORNFLOWERBLUE = register("CORNFLOWERBLUE", 0X6495ED);
    public static final Color CORNSILK = register("CORNSILK", 0XFFF8DC);
    public static final Color CRIMSON = register("CRIMSON", 0XDC143C);
    public static final Color DARKBLUE = register("DARKBLUE", 0X00008B);
    public static final Color DARKCYAN = register("DARKCYAN", 0X008B8B);
    public static final Color DARKGOLDENROD = register("DARKGOLDENROD", 0XB8860B);
    public static final Color DARKGRAY = register("DARKGRAY", 0XA9A9A9);
    public static final Color DARKGREEN = register("DARKGREEN", 0X006400);
    public static final Color DARKGREY = register("DARKGREY", 0XA9A9A9);
    public static final Color DARKKHAKI = register("DARKKHAKI", 0XBDB76B);
    public static final Color DARKMAGENTA = register("DARKMAGENTA", 0X8B008B);
    public static final Color DARKOLIVEGREEN = register("DARKOLIVEGREEN", 0X556B2F);
    public static final Color DARKORANGE = register("DARKORANGE", 0XFF8C00);
    public static final Color DARKORCHID = register("DARKORCHID", 0X9932CC);
    public static final Color DARKRED = register("DARKRED", 0X8B0000);
    public static final Color DARKSALMON = register("DARKSALMON", 0XE9967A);
    public static final Color DARKSEAGREEN = register("DARKSEAGREEN", 0X8FBC8F);
    public static final Color DARKSLATEBLUE = register("DARKSLATEBLUE", 0X483D8B);
    public static final Color DARKSLATEGRAY = register("DARKSLATEGRAY", 0X2F4F4F);
    public static final Color DARKSLATEGREY = register("DARKSLATEGREY", 0X2F4F4F);
    public static final Color DARKTURQUOISE = register("DARKTURQUOISE", 0X00CED1);
    public static final Color DARKVIOLET = register("DARKVIOLET", 0X9400D3);
    public static final Color DEEPPINK = register("DEEPPINK", 0XFF1493);
    public static final Color DEEPSKYBLUE = register("DEEPSKYBLUE", 0X00BFFF);
    public static final Color DIMGRAY = register("DIMGRAY", 0X696969);
    public static final Color DIMGREY = register("DIMGREY", 0X696969);
    public static final Color DODGERBLUE = register("DODGERBLUE", 0X1E90FF);
    public static final Color FIREBRICK = register("FIREBRICK", 0XB22222);
    public static final Color FLORALWHITE = register("FLORALWHITE", 0XFFFAF0);
    public static final Color FORESTGREEN = register("FORESTGREEN", 0X228B22);
    public static final Color GAINSBORO = register("GAINSBORO", 0XDCDCDC);
    public static final Color GHOSTWHITE = register("GHOSTWHITE", 0XF8F8FF);
    public static final Color GOLD = register("GOLD", 0XFFD700);
    public static final Color GOLDENROD = register("GOLDENROD", 0XDAA520);
    public static final Color GREENYELLOW = register("GREENYELLOW", 0XADFF2F);
    public static final Color GREY = register("GREY", 0X808080);
    public static final Color HONEYDEW = register("HONEYDEW", 0XF0FFF0);
    public static final Color HOTPINK = register("HOTPINK", 0XFF69B4);
    public static final Color INDIANRED = register("INDIANRED", 0XCD5C5C);
    public static final Color INDIGO = register("INDIGO", 0X4B0082);
    public static final Color IVORY = register("IVORY", 0XFFFFF0);
    public static final Color KHAKI = register("KHAKI", 0XF0E68C);
    public static final Color LAVENDER = register("LAVENDER", 0XE6E6FA);
    public static final Color LAVENDERBLUSH = register("LAVENDERBLUSH", 0XFFF0F5);
    public static final Color LAWNGREEN = register("LAWNGREEN", 0X7CFC00);
    public static final Color LEMONCHIFFON = register("LEMONCHIFFON", 0XFFFACD);
    public static final Color LIGHTBLUE = register("LIGHTBLUE", 0XADD8E6);
    public static final Color LIGHTCORAL = register("LIGHTCORAL", 0XF08080);
    public static final Color LIGHTCYAN = register("LIGHTCYAN", 0XE0FFFF);
    public static final Color LIGHTGOLDENRODYELLOW = register("LIGHTGOLDENRODYELLOW", 0XFAFAD2);
    public static final Color LIGHTGRAY = register("LIGHTGRAY", 0XD3D3D3);
    public static final Color LIGHTGREEN = register("LIGHTGREEN", 0X90EE90);
    public static final Color LIGHTGREY = register("LIGHTGREY", 0XD3D3D3);
    public static final Color LIGHTPINK = register("LIGHTPINK", 0XFFB6C1);
    public static final Color LIGHTSALMON = register("LIGHTSALMON", 0XFFA07A);
    public static final Color LIGHTSEAGREEN = register("LIGHTSEAGREEN", 0X20B2AA);
    public static final Color LIGHTSKYBLUE = register("LIGHTSKYBLUE", 0X87CEFA);
    public static final Color LIGHTSLATEGRAY = register("LIGHTSLATEGRAY", 0X778899);
    public static final Color LIGHTSLATEGREY = register("LIGHTSLATEGREY", 0X778899);
    public static final Color LIGHTSTEELBLUE = register("LIGHTSTEELBLUE", 0XB0C4DE);
    public static final Color LIGHTYELLOW = register("LIGHTYELLOW", 0XFFFFE0);
    public static final Color LIMEGREEN = register("LIMEGREEN", 0X32CD32);
    public static final Color LINEN = register("LINEN", 0XFAF0E6);
    public static final Color MEDIUMAQUAMARINE = register("MEDIUMAQUAMARINE", 0X66CDAA);
    public static final Color MEDIUMBLUE = register("MEDIUMBLUE", 0X0000CD);
    public static final Color MEDIUMORCHID = register("MEDIUMORCHID", 0XBA55D3);
    public static final Color MEDIUMPURPLE = register("MEDIUMPURPLE", 0X9370DB);
    public static final Color MEDIUMSEAGREEN = register("MEDIUMSEAGREEN", 0X3CB371);
    public static final Color MEDIUMSLATEBLUE = register("MEDIUMSLATEBLUE", 0X7B68EE);
    public static final Color MEDIUMSPRINGGREEN = register("MEDIUMSPRINGGREEN", 0X00FA9A);
    public static final Color MEDIUMTURQUOISE = register("MEDIUMTURQUOISE", 0X48D1CC);
    public static final Color MEDIUMVIOLETRED = register("MEDIUMVIOLETRED", 0XC71585);
    public static final Color MIDNIGHTBLUE = register("MIDNIGHTBLUE", 0X191970);
    public static final Color MINTCREAM = register("MINTCREAM", 0XF5FFFA);
    public static final Color MISTYROSE = register("MISTYROSE", 0XFFE4E1);
    public static final Color MOCCASIN = register("MOCCASIN", 0XFFE4B5);
    public static final Color NAVAJOWHITE = register("NAVAJOWHITE", 0XFFDEAD);
    public static final Color OLDLACE = register("OLDLACE", 0XFDF5E6);
    public static final Color OLIVEDRAB = register("OLIVEDRAB", 0X6B8E23);
    public static final Color ORANGERED = register("ORANGERED", 0XFF4500);
    public static final Color ORCHID = register("ORCHID", 0XDA70D6);
    public static final Color PALEGOLDENROD = register("PALEGOLDENROD", 0XEEE8AA);
    public static final Color PALEGREEN = register("PALEGREEN", 0X98FB98);
    public static final Color PALETURQUOISE = register("PALETURQUOISE", 0XAFEEEE);
    public static final Color PALEVIOLETRED = register("PALEVIOLETRED", 0XDB7093);
    public static final Color PAPAYAWHIP = register("PAPAYAWHIP", 0XFFEFD5);
    public static final Color PEACHPUFF = register("PEACHPUFF", 0XFFDAB9);
    public static final Color PERU = register("PERU", 0XCD853F);
    public static final Color PINK = register("PINK", 0XFFC0CB);
    public static final Color PLUM = register("PLUM", 0XDDA0DD);
    public static final Color POWDERBLUE = register("POWDERBLUE", 0XB0E0E6);
    public static final Color ROSYBROWN = register("ROSYBROWN", 0XBC8F8F);
    public static final Color ROYALBLUE = register("ROYALBLUE", 0X4169E1);
    public static final Color SADDLEBROWN = register("SADDLEBROWN", 0X8B4513);
    public static final Color SALMON = register("SALMON", 0XFA8072);
    public static final Color SANDYBROWN = register("SANDYBROWN", 0XF4A460);
    public static final Color SEAGREEN = register("SEAGREEN", 0X2E8B57);
    public static final Color SEASHELL = register("SEASHELL", 0XFFF5EE);
    public static final Color SIENNA = register("SIENNA", 0XA0522D);
    public static final Color SKYBLUE = register("SKYBLUE", 0X87CEEB);
    public static final Color SLATEBLUE = register("SLATEBLUE", 0X6A5ACD);
    public static final Color SLATEGRAY = register("SLATEGRAY", 0X708090);
    public static final Color SLATEGREY = register("SLATEGREY", 0X708090);
    public static final Color SNOW = register("SNOW", 0XFFFAFA);
    public static final Color SPRINGGREEN = register("SPRINGGREEN", 0X00FF7F);
    public static final Color STEELBLUE = register("STEELBLUE", 0X4682B4);
    public static final Color TAN = register("TAN", 0XD2B48C);
    public static final Color THISTLE = register("THISTLE", 0XD8BFD8);
    public static final Color TOMATO = register("TOMATO", 0XFF6347);
    public static final Color TURQUOISE = register("TURQUOISE", 0X40E0D0);
    public static final Color VIOLET = register("VIOLET", 0XEE82EE);
    public static final Color WHEAT = register("WHEAT", 0XF5DEB3);
    public static final Color WHITESMOKE = register("WHITESMOKE", 0XF5F5F5);
    public static final Color YELLOWGREEN = register("YELLOWGREEN", 0X9ACD32);
    public static final Color REBECCAPURPLE = register("REBECCAPURPLE", 0X663399);

    private final int argb;

    private static int shiftComponentValue(int value, int bits) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException();
        }
        return value << bits;
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

    private static final int SHIFT_A = 24;
    private static final int SHIFT_R = 16;
    private static final int SHIFT_G = 8;
    private static final int SHIFT_B = 0;

    private Color(int argb) {
        this.argb = argb;
    }

    public int argb() {
        return argb;
    }

    public int r() {
        return (argb >> SHIFT_R) & 0xff;
    }

    public int g() {
        return (argb >> SHIFT_G) & 0xff;
    }

    public int b() {
        return (argb >> SHIFT_B) & 0xff;
    }

    public int a() {
        return (argb >> SHIFT_A) & 0xff;
    }

    public float rf() {
        return r() / 255f;
    }

    public float gf() {
        return g() / 255f;
    }

    public float bf() {
        return b() / 255f;
    }

    public float af() {
        return a() / 255f;
    }

    public byte[] toByteArray() {
        byte[] arr = {
            (byte) r(), (byte) g(), (byte) b(), (byte) a()
        };
        return arr;
    }

    private static Color register(String name, int code) {
        Color c = new Color(code);
        COLORS.put(name, c);
        return c;
    }

    public static Color valueOf(String s) {
        // try named colors first
        Color color = COLORS.get(s);
        if (color != null) {
            return color;
        }

        // HEX colors
        if (s.startsWith("#")) {
            // FIXME JDK 8
            // int i = Integer.parseUnsignedInt(s.substring(1), 16);
            int i = (int) Long.parseLong(s.substring(1), SHIFT_R);
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

    @Override
    public String toString() {
        return "#" + Integer.toHexString(argb);
    }

    private static final double F_BRIGHTEN = 0.7;

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
                Math.max((int) (r() * F_BRIGHTEN), 0),
                Math.max((int) (r() * F_BRIGHTEN), 0),
                a());
    }
}
