module com.dua3.meja.fx {
    exports com.dua3.meja.ui.fx;
    opens com.dua3.meja.ui.fx;

    requires javafx.controls;

    requires transitive com.dua3.meja;
    requires com.dua3.utility;
}
