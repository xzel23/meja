module meja.swing {
    exports com.dua3.meja.ui;
    exports com.dua3.meja.ui.swing;

    requires transitive meja;

    requires java.desktop;
    requires java.logging;
    requires dua3_utility;
    requires dua3_utility.swing;
}
