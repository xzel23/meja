/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
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
package com.dua3.meja.ui.swing;

import com.dua3.utility.math.MathUtil;
import com.dua3.utility.swing.TextEditorPane;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.RichTextEditorModel;

import java.util.Map;

/**
 * Text editor wrapper that maps font-size attributes between logical points
 * (model/cell data) and editor display scale.
 */
final class ScaledTextEditorPane extends TextEditorPane {
    private double fontScale = 1.0;

    void setFontScale(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException("font scale must be > 0: " + value);
        }
        this.fontScale = value;
    }

    void setCellText(RichText value) {
        super.setText(scaleFontSizes(value, fontScale));
    }

    @Override
    public RichText getText() {
        return scaleFontSizes(super.getText(), 1.0 / fontScale);
    }

    @Override
    public double getFontSize() {
        return MathUtil.round(super.getFontSize() / fontScale, 2);
    }

    @Override
    public void setFontSize(double value) {
        super.setFontSize(value * fontScale);
    }

    private static RichText scaleFontSizes(RichText value, double factor) {
        if (!Double.isFinite(factor) || factor <= 0.0 || Math.abs(factor - 1.0) < 1.0e-6 || value.isEmpty()) {
            return value;
        }

        RichText result = value;
        int start = 0;
        for (Run run : value) {
            int end = start + run.length();
            double size = RichTextEditorModel.resolveFontSize(run.attributes(), run.getStyles());
            if (Double.isFinite(size) && size > 0.0) {
                double scaled = size * factor;
                if (Double.isFinite(scaled) && scaled > 0.0) {
                    result = result.apply(Map.of(Style.FONT_SIZE, (float) scaled), start, end);
                }
            }
            start = end;
        }

        return result;
    }
}
