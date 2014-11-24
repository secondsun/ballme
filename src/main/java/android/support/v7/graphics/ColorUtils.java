/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v7.graphics;

final class ColorUtils {

    private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
    private static final int MIN_ALPHA_SEARCH_PRECISION = 10;

    public static final int BLACK       = 0xFF000000;
    public static final int DKGRAY      = 0xFF444444;
    public static final int GRAY        = 0xFF888888;
    public static final int LTGRAY      = 0xFFCCCCCC;
    public static final int WHITE       = 0xFFFFFFFF;
    public static final int RED         = 0xFFFF0000;
    public static final int GREEN       = 0xFF00FF00;
    public static final int BLUE        = 0xFF0000FF;
    public static final int YELLOW      = 0xFFFFFF00;
    public static final int CYAN        = 0xFF00FFFF;
    public static final int MAGENTA     = 0xFFFF00FF;
    public static final int TRANSPARENT = 0;
    
    private ColorUtils() {}

    /**
     * Composite two potentially translucent colors over each other and returns the result.
     */
    private static int compositeColors(int fg, int bg) {
        final float alpha1 = alpha(fg) / 255f;
        final float alpha2 = alpha(bg) / 255f;

        float a = (alpha1 + alpha2) * (1f - alpha1);
        float r = (red(fg) * alpha1) + (red(bg) * alpha2 * (1f - alpha1));
        float g = (green(fg) * alpha1) + (green(bg) * alpha2 * (1f - alpha1));
        float b = (blue(fg) * alpha1) + (blue(bg) * alpha2 * (1f - alpha1));

        return argb((int) a, (int) r, (int) g, (int) b);
    }

    /**
     * Returns the luminance of a color.
     *
     * Formula defined here: http://www.w3.org/TR/2008/REC-WCAG20-20081211/#relativeluminancedef
     */
    private static double calculateLuminance(int color) {
        double red = red(color) / 255d;
        red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);

        double green = green(color) / 255d;
        green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);

        double blue = blue(color) / 255d;
        blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);

        return (0.2126 * red) + (0.7152 * green) + (0.0722 * blue);
    }

    /**
     * Returns the contrast ratio between two colors.
     *
     * Formula defined here: http://www.w3.org/TR/2008/REC-WCAG20-20081211/#contrast-ratiodef
     */
    private static double calculateContrast(int foreground, int background) {
        if (alpha(background) != 255) {
            throw new IllegalArgumentException("background can not be translucent");
        }
        if (alpha(foreground) < 255) {
            // If the foreground is translucent, composite the foreground over the background
            foreground = compositeColors(foreground, background);
        }

        final double luminance1 = calculateLuminance(foreground) + 0.05;
        final double luminance2 = calculateLuminance(background) + 0.05;

        // Now return the lighter luminance divided by the darker luminance
        return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
    }

    /**
     * Finds the minimum alpha value which can be applied to {@code foreground} so that is has a
     * contrast value of at least {@code minContrastRatio} when compared to background.
     *
     * @return the alpha value in the range 0-255.
     */
    private static int findMinimumAlpha(int foreground, int background, double minContrastRatio) {
        if (alpha(background) != 255) {
            throw new IllegalArgumentException("background can not be translucent");
        }

        // First lets check that a fully opaque foreground has sufficient contrast
        int testForeground = modifyAlpha(foreground, 255);
        double testRatio = calculateContrast(testForeground, background);
        if (testRatio < minContrastRatio) {
            // Fully opaque foreground does not have sufficient contrast, return error
            return -1;
        }

        // Binary search to find a value with the minimum value which provides sufficient contrast
        int numIterations = 0;
        int minAlpha = 0;
        int maxAlpha = 255;

        while (numIterations <= MIN_ALPHA_SEARCH_MAX_ITERATIONS &&
                (maxAlpha - minAlpha) > MIN_ALPHA_SEARCH_PRECISION) {
            final int testAlpha = (minAlpha + maxAlpha) / 2;

            testForeground = modifyAlpha(foreground, testAlpha);
            testRatio = calculateContrast(testForeground, background);

            if (testRatio < minContrastRatio) {
                minAlpha = testAlpha;
            } else {
                maxAlpha = testAlpha;
            }

            numIterations++;
        }

        // Conservatively return the max of the range of possible alphas, which is known to pass.
        return maxAlpha;
    }

    static int getTextColorForBackground(int backgroundColor, float minContrastRatio) {
        // First we will check white as most colors will be dark
        final int whiteMinAlpha = ColorUtils
                .findMinimumAlpha(WHITE, backgroundColor, minContrastRatio);

        if (whiteMinAlpha >= 0) {
            return ColorUtils.modifyAlpha(WHITE, whiteMinAlpha);
        }

        // If we hit here then there is not an translucent white which provides enough contrast,
        // so check black
        final int blackMinAlpha = ColorUtils
                .findMinimumAlpha(BLACK, backgroundColor, minContrastRatio);

        if (blackMinAlpha >= 0) {
            return ColorUtils.modifyAlpha(BLACK, blackMinAlpha);
        }

        // This should not happen!
        return -1;
    }

    static void RGBtoHSL(int r, int g, int b, float[] hsl) {
        final float rf = r / 255f;
        final float gf = g / 255f;
        final float bf = b / 255f;

        final float max = Math.max(rf, Math.max(gf, bf));
        final float min = Math.min(rf, Math.min(gf, bf));
        final float deltaMaxMin = max - min;

        float h, s;
        float l = (max + min) / 2f;

        if (max == min) {
            // Monochromatic
            h = s = 0f;
        } else {
            if (max == rf) {
                h = ((gf - bf) / deltaMaxMin) % 6f;
            } else if (max == gf) {
                h = ((bf - rf) / deltaMaxMin) + 2f;
            } else {
                h = ((rf - gf) / deltaMaxMin) + 4f;
            }

            s =  deltaMaxMin / (1f - Math.abs(2f * l - 1f));
        }

        hsl[0] = (h * 60f) % 360f;
        hsl[1] = s;
        hsl[2] = l;
    }

    static int HSLtoRGB (float[] hsl) {
        final float h = hsl[0];
        final float s = hsl[1];
        final float l = hsl[2];

        final float c = (1f - Math.abs(2 * l - 1f)) * s;
        final float m = l - 0.5f * c;
        final float x = c * (1f - Math.abs((h / 60f % 2f) - 1f));

        final int hueSegment = (int) h / 60;

        int r = 0, g = 0, b = 0;

        switch (hueSegment) {
            case 0:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * (x + m));
                b = Math.round(255 * m);
                break;
            case 1:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * (c + m));
                b = Math.round(255 * m);
                break;
            case 2:
                r = Math.round(255 * m);
                g = Math.round(255 * (c + m));
                b = Math.round(255 * (x + m));
                break;
            case 3:
                r = Math.round(255 * m);
                g = Math.round(255 * (x + m));
                b = Math.round(255 * (c + m));
                break;
            case 4:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (c + m));
                break;
            case 5:
            case 6:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (x + m));
                break;
        }

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return rgb(r, g, b);
    }

    /**
     * Set the alpha component of {@code color} to be {@code alpha}.
     */
    static int modifyAlpha(int color, int alpha) {
        return (color & 0x00ffffff) | (alpha << 24);
    }
    
     /**
     * Return the alpha component of a color int. This is the same as saying
     * color >>> 24
     */
    public static int alpha(int color) {
        return color >>> 24;
    }

    /**
     * Return the red component of a color int. This is the same as saying
     * (color >> 16) & 0xFF
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Return the green component of a color int. This is the same as saying
     * (color >> 8) & 0xFF
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Return the blue component of a color int. This is the same as saying
     * color & 0xFF
     */
    public static int blue(int color) {
        return color & 0xFF;
    }

    /**
     * Return a color-int from red, green, blue components.
     * The alpha component is implicity 255 (fully opaque).
     * These component values should be [0..255], but there is no
     * range check performed, so if they are out of range, the
     * returned color is undefined.
     * @param red  Red component [0..255] of the color
     * @param green Green component [0..255] of the color
     * @param blue  Blue component [0..255] of the color
     */
    public static int rgb(int red, int green, int blue) {
        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Return a color-int from alpha, red, green, blue components.
     * These component values should be [0..255], but there is no
     * range check performed, so if they are out of range, the
     * returned color is undefined.
     * @param alpha Alpha component [0..255] of the color
     * @param red   Red component [0..255] of the color
     * @param green Green component [0..255] of the color
     * @param blue  Blue component [0..255] of the color
     */
    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
