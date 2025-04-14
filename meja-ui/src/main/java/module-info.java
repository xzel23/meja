import org.jspecify.annotations.NullMarked;

/**
 * The com.dua3.meja.ui module provides interface definitions for the com.dua3.meja library's UI classes.
 */
@NullMarked
module com.dua3.meja.ui {
    exports com.dua3.meja.ui;

    requires transitive com.dua3.meja;

    requires com.dua3.utility;

    requires org.jspecify;
    requires org.apache.logging.log4j;
}
