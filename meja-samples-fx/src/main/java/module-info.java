import org.jspecify.annotations.NullMarked;

@NullMarked
open module com.dua3.meja.fx.samples {
    exports com.dua3.meja.fx.samples;

    requires com.dua3.fx.application;
    requires com.dua3.meja.generic;
    requires com.dua3.meja.fx;
    requires com.dua3.utility;
    requires com.dua3.utility.logging;
    requires com.dua3.utility.logging.log4j;
    requires com.dua3.utility.fx;
    requires com.dua3.utility.fx.controls;
    requires com.dua3.utility.fx.icons;
    requires org.apache.logging.log4j;
    requires org.jspecify;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
}
