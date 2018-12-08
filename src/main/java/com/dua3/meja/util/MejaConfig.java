package com.dua3.meja.util;

public class MejaConfig {

    private static final MejaConfig INSTANCE = new MejaConfig();

    /**
     * The property name used to explicitly enable/disable XOR drawing mode.
     */
    public static final String MEJA_USE_XOR_DRAWING = "MEJA_USE_XOR_DRAWING";

    /**
     * Check if XOR mode enabled for drawing the selection rectangle.
     * 
     * @return true, if XOR drawing is enabled
     */
    public static boolean isXorDrawModeEnabled() {
        return INSTANCE.useXorDrawMode;
    }

    /**
     * Flag to control whether or not XOR mode is used when drawing the selection
     * rectangle.
     */
    private final boolean useXorDrawMode;

    {
        String propertyXorDrawing = System.getProperty(MEJA_USE_XOR_DRAWING);
        if (propertyXorDrawing != null) {
            useXorDrawMode = Boolean.valueOf(propertyXorDrawing);
        } else {
            // default is enabled except for linux
            useXorDrawMode = !System.getProperty("os.name").toLowerCase().contains("linux");
        }
    }

    private MejaConfig() {
    }

}
