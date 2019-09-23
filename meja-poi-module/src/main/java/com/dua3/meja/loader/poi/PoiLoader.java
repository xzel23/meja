package com.dua3.meja.loader.poi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.utility.options.OptionValues;
import com.dua3.utility.lang.LangUtil;

/**
 * Load and Provide the PoiWorkbookFactory.
 * <p>
 * This class creates a Classloader and loads the implementation from its own
 * resource path. This makes it possible to use the Apache POI implementation in
 * a fully modularized project although POI itself still uses automatic modules.
 */
public class PoiLoader extends WorkbookFactory<Workbook> {

    private static final Logger LOG = Logger.getLogger(PoiLoader.class.getName());

    private final WorkbookFactory<? extends Workbook> factory;

    public PoiLoader() {
        try {
            Properties properties = new Properties();
            try (InputStream in = PoiLoader.class.getResourceAsStream("lib/files.properties")) {
                properties.load(in);
            }

            String filesStr = properties.getOrDefault("files", "[]").toString().trim();

            LangUtil.check(filesStr.startsWith("[") && filesStr.endsWith("]"));
            filesStr = filesStr.substring(1,filesStr.length()-1);

            URL[] urls = Arrays.stream(filesStr.split(","))
                .map(String::trim)
                .map(file -> PoiLoader.class.getResource("lib/"+file))
                .toArray(URL[]::new);

            LOG.info(() -> "creating classloader with content: "+Arrays.toString(urls));

            URLClassLoader classloader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls));

            this.factory = (WorkbookFactory<? extends Workbook>) classloader.loadClass("com.dua3.meja.model.poi.PoiWorkbookFactory").getConstructor().newInstance();
        } catch (ClassNotFoundException
            | NoSuchMethodException
            | InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | IOException e) {
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
    public Workbook open(URI uri, OptionValues importSettings) throws IOException {
        return factory.open(uri, importSettings);
    }
}
