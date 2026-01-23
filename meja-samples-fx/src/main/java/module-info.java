import org.jspecify.annotations.NullMarked;

/**
 * This module provides JavaFX sample applications for Meja.
 */
@NullMarked
open module com.dua3.meja.fx.samples {
    exports com.dua3.meja.fx.samples;

    requires com.dua3.meja.generic;
    requires com.dua3.meja.fx;
    requires com.dua3.meja.poi;
    requires com.dua3.utility;
    requires com.dua3.utility.fx;
    requires com.dua3.utility.fx.controls;
    requires org.apache.logging.log4j;
    requires org.jspecify;
    requires org.slb4j;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
}
