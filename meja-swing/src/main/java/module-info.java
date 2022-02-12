module com.dua3.meja.swing {
    exports com.dua3.meja.ui;
    exports com.dua3.meja.ui.swing;

    requires transitive com.dua3.meja;

    requires java.desktop;
    requires java.logging;
    requires com.dua3.utility;
    requires com.dua3.utility.swing;
    
    requires static com.dua3.cabe.annotations;
}
