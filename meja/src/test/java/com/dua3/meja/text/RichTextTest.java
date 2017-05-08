/*
 * Copyright 2016 Axel Howind.
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
package com.dua3.meja.text;

import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;

import org.junit.Test;

/**
 *
 * @author Axel Howind
 */
public class RichTextTest {

    public RichTextTest() {
    }

    @Test
    public void testRichTextBuilding() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, "bold");
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("!");

        RichText rt = builder.toRichText();
        assertEquals("Hello world!", rt.toString());
        assertEquals("Hello world!", rt.stream().collect(Collectors.joining()));
    }

}
