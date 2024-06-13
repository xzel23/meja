package com.dua3.meja.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.text.VerticalAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public interface Graphics {

    FontUtil<?> getFontUtil();

    /**
     * Get bounds.
     *
     * @return the bounding rectangle
     */
    Rectangle2f getBounds();

    /**
     * Get text dimensions using the current font.
     *
     * @return the text dimensions
     */
    Rectangle2f getTextDimension(CharSequence text);

    /**
     * Start drawing.
     */
    void beginDraw();

    /**
     * End drawing and release ressources.
     */
    void endDraw();

    /**
     * Stroke rectangle.
     * @param r the recatngle
     */
    default void strokeRect(Rectangle2f r) {
        strokeRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    void strokeRect(float x, float y, float w, float h);

    /**
     * Fill rectangle.
     *
     * @param r the rectangle
     */
    default void fillRect(Rectangle2f r) {
        fillRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    void fillRect(float x, float y, float w, float h);

    default void strokeLine(Vector2f a, Vector2f b) {
        strokeLine(a.x(), a.y(), b.x(), b.y());
    }

    void strokeLine(float x1, float y1, float x2, float y2);

    void setStroke(Color c, float width);

    void setFill(Color c);

    void setTransformation(AffineTransformation2f t);

    void setFont(Font f);

    void drawText(CharSequence text, float x, float y);

    default Rectangle2f getBoundsInLocal() {
        AffineTransformation2f ti = getTransformation().inverse().orElseThrow();
        Rectangle2f bounds = getBounds();
        return Rectangle2f.withCorners(
                ti.transform(bounds.min()),
                ti.transform(bounds.max())
        );
    }

    enum HAnchor {
        LEFT, RIGHT, CENTER
    }

    enum VAnchor {
        TOP, BOTTOM, BASELINE, MIDDLE
    }

    default void drawText(CharSequence text, float x, float y, HAnchor hAnchor, VAnchor vAnchor) {
        Rectangle2f r = getTextDimension(text);

        float tx, ty;

        tx = switch (hAnchor) {
            case LEFT -> x;
            case RIGHT -> x - r.width();
            case CENTER -> x - r.width() / 2;
        };

        ty = switch (vAnchor) {
            case TOP -> y - r.yMin();
            case BOTTOM -> y - r.yMax();
            case BASELINE -> y + r.height() - r.y();
            case MIDDLE -> y + r.height() / 2 - r.yMax();
        };

        drawText(text, tx, ty);
    }


    AffineTransformation2f getTransformation();

    default void renderText(Rectangle2f r, RichText text, Font font, Alignment hAlign, VerticalAlignment vAlign, boolean wrapping) {
        FragmentedText fragments = generateFragments(text, r, font, hAlign, vAlign, wrapping);
        renderFragments(r, hAlign, vAlign, fragments.textWidth(), fragments.textHeight(), fragments.baseLine(), fragments.fragmentLines());
    }

    /**
     * A text fragment that can't be split (i.e., contains no whitespace) and has a uniform font so that it can be
     * drawn in a single operation.
     *
     * @param x the x-position
     * @param y the y-position
     * @param w the width
     * @param h the height
     * @param baseLine the basline value (of the line the fragment belongs to
     * @param font the font
     * @param text the text
     */
    record Fragment (float x, float y, float w, float h, float baseLine, Font font, CharSequence text) {}

    record FragmentedText(List<List<Fragment>> fragmentLines, float textWidth, float textHeight, float baseLine) {}

    /**
     * Split text into fragments.
     *
     * <p>Split the text into fragments that are eihther whitespcae or free of whitespace and have uniform
     * text attributes (font, text decoration). For each line, a list of such fragments is generated and added
     * to the list of fragmet lines (see {@link FragmentedText#fragmentLines()}).
     *
     * @param text      the text
     * @param r         the bounding rectangle to render the text into
     * @param font      the default font
     * @param hAlign    the horizontal alignment
     * @param vAlign    thee vertical alignment
     * @param wrap      if wrapping should be applied (
     * @return
     */
    private FragmentedText generateFragments(RichText text, Rectangle2f r, Font font, Alignment hAlign, VerticalAlignment vAlign, boolean wrap) {
        float wrapWidth = wrap ? r.width() : Float.MAX_VALUE;

        Function<RichText, RichText> trimLine = switch (hAlign) {
            case LEFT -> RichText::stripTrailing;
            case RIGHT -> RichText::stripLeading;
            case CENTER, JUSTIFY -> RichText::strip;
        };

        // generate lists of chunks for each line
        FontUtil<?> fontUtil = getFontUtil();
        List<List<Fragment>> fragmentLines = new ArrayList<>();
        float textWidth = 0f;
        float textHeight = 0f;
        float baseLine = 0f;
        for (RichText line: text.split("\n")) {
            line = trimLine.apply(line);

            List<Fragment> fragments = new ArrayList<>();
            fragmentLines.add(fragments);

            float xAct = 0f;
            float lineHeight = 0f;
            float lineWidth = 0f;
            float lineBaseLine = 0f;
            boolean wrapAllowed = false;
            for (var run: splitLinePreservingWhitespace(line, wrap)) {
                Font f = font.deriveFont(run.getFontDef());
                Rectangle2f tr = fontUtil.getTextDimension(run, f);
                if (wrapAllowed && xAct + tr.width() > wrapWidth) {
                    if (!fragments.isEmpty() && TextUtil.isBlank(fragments.get(fragments.size() - 1).text())) {
                        // remove trailing whitespace
                        fragments.remove(fragments.size() - 1);
                    } else if (TextUtil.isBlank(run)) {
                        // skip leading whitespace after wrapped line
                        continue;
                    }
                    fragments = new ArrayList<>();
                    fragmentLines.add(fragments);
                    xAct = 0f;
                    textHeight += lineHeight;
                    fragments.add(new Fragment(xAct, textHeight, tr.width(), tr.height(), lineBaseLine, f, run));
                    xAct += tr.width();
                    lineWidth = tr.width();
                    lineHeight = tr.height();
                    wrapAllowed = false;
                    lineBaseLine = -tr.yMin();
                } else {
                    wrapAllowed = wrap;
                    fragments.add(new Fragment(xAct, textHeight, tr.width(), tr.height(), lineBaseLine, f, run));
                    xAct += tr.width();
                    lineWidth += tr.width();
                    lineHeight = Math.max(lineHeight, tr.height());
                    lineBaseLine = Math.max(lineBaseLine, -tr.yMin());
                }
            }
            textWidth = Math.max(textWidth, lineWidth);
            textHeight += lineHeight;
            baseLine = lineBaseLine;
        }
        return new FragmentedText(fragmentLines, textWidth, textHeight, baseLine);
    }

    private void renderFragments(Rectangle2f cr, Alignment hAlign, VerticalAlignment vAlign, float textWidth, float textHeight, float baseLine, List<List<Fragment>> fragmentLines) {
        float y = switch (vAlign) {
            case TOP, DISTRIBUTED -> cr.yMin();
            case MIDDLE -> cr.yCenter() - textHeight /2;
            case BOTTOM -> y = cr.yMax() - textHeight;
        };
        float fillerHeight = vAlign == VerticalAlignment.DISTRIBUTED ?  (cr.height()- textHeight)/Math.max(1, fragmentLines.size()-1) : 0f;

        record LineStatistics(float text, float whiteSpace, int nSpace) {}
        for (List<Fragment> fragments : fragmentLines) {
            // determine the number and size of whitespace and text fragments
            LineStatistics fi = fragments.stream().map(fragment -> {
                        boolean isWS = TextUtil.isBlank(fragment.text());
                        return new LineStatistics(isWS ? 0f: fragment.w(), isWS ? fragment.w() : 0f, isWS ? 1 : 0);
                    })
                    .reduce((a,b) -> new LineStatistics(a.text + b.text, a.whiteSpace + b.whiteSpace, a.nSpace + b.nSpace))
                    .orElseGet(() -> new LineStatistics(0f, 0f, 1));

            float spaceToDistribute = cr.width() - fi.text - fi.whiteSpace;
            float totalSpace = fi.whiteSpace + spaceToDistribute;

            float x= cr.xMin();
            for (Fragment fragment : fragments) {
                switch (hAlign) {
                    case JUSTIFY -> {
                        // distribute remaining space by evenly expanding existind whitespace
                        if (TextUtil.isBlank(fragment.text())) {
                            x += fragment.w() * (totalSpace / fi.whiteSpace() - 1);
                        }
                    }
                    case RIGHT -> {
                        if (fragment.x() == 0f) {
                            // push everything to the right
                            x += spaceToDistribute;
                        }
                    }
                    case CENTER -> {
                        if (fragment.x() == 0f) {
                            // push everything halfway right
                            x += spaceToDistribute/2f;
                        }
                    }
                    case LEFT -> { /* nothing to do */}
                }
                setFont(fragment.font);
                drawText(fragment.text.toString(), x + fragment.x, y + fragment.y + baseLine);
            }
            y += fillerHeight;
        }
    }

    private static List<Run> splitLinePreservingWhitespace(RichText line, boolean wrapping) {
        if (!wrapping) {
            return line.runs();
        }

        return Arrays.stream(line.split("(?<=\\s)|(?=\\s)"))
                .flatMap(part -> part.runs().stream())
                .toList();
    }

}