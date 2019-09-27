module meja.fx {
    exports com.dua3.meja.util.fx;
    opens com.dua3.meja.util.fx;

    requires javafx.controls;

    requires transitive meja;
    requires dua3_utility;
}
