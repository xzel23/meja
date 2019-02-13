package com.dua3.meja.loader.poi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.Options;

/**
 * Load and Provide the PoiWorkbookFactory.
 * <p>
 * This class creates a Classloader and loads the implementation from its own resource path.
 * This makes it possible to use the Apache POI implementation in a fully modularized project
 * although POI itself still uses automatic modules.
 */
public class PoiLoader extends WorkbookFactory<Workbook> {

    private static final Logger LOG = Logger.getLogger(PoiLoader.class.getName());

    private final WorkbookFactory<? extends Workbook> factory;

    public PoiLoader() {
            LOG.fine("loading factory class");
            URLClassLoader classloader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
            @Override
            public URLClassLoader run() {
                return new URLClassLoader(new URL[] { PoiLoader.class.getResource("lib/") });
            }
            });
            try {
                this.factory = (WorkbookFactory<? extends Workbook>) classloader.loadClass("com.dua3.meja.poi.PoiWorkbookFactory").getConstructor().newInstance();
            } catch (ClassNotFoundException|NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

    @Override
    public Workbook create() {
        return factory.create();
    }

    @Override
    public Workbook createStreaming() {
        return factory.createStreaming();
    }

    @Override
    public Workbook open(Path path, Options importSettings) throws IOException {
        return factory.open(path, importSettings);
    }
}
