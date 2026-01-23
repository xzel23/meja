import org.jspecify.annotations.NullMarked;

/**
 * A module containing different sample applications.
 */
@NullMarked
module com.dua3.meja.samples {
    requires java.desktop;
    requires com.dua3.meja.generic;
    requires com.dua3.meja.swing;
    requires com.dua3.utility;
    requires com.dua3.utility.swing;
    requires org.apache.logging.log4j;
    requires org.jspecify;
    requires org.slb4j;
}
