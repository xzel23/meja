package com.dua3.meja.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import com.dua3.meja.util.ObjectCache;

public abstract class AbstractWorkbook
        implements Workbook {

    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
    * The path of this workbook.
    */
    protected Path path;

    /**
     * The object cache used to reduce memory usage.
     */
    private ObjectCache objectCache = null;

    protected final Locale locale;

    public AbstractWorkbook(Locale locale, Path path) {
        this.locale = locale;
        this.path = path;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public <T> T cache(T obj) {
        return objectCache != null ? objectCache.get(obj) : obj;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Optional<Path> getPath() {
        return Optional.ofNullable(path);
    }

    /**
     * Check if object caching is enabled.
     *
     * @return true if object caching is enabled
     */
    @Override
    public boolean isObjectCachingEnabled() {
        return objectCache != null;
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Enable or disable object caching.
     *
     * @param enabled
     *            true, if the object cache should be enabled false, if objects
     *            should not be cached anymore and the cash cleared
     */
    @Override
    public void setObjectCaching(boolean enabled) {
        if (enabled && !isObjectCachingEnabled()) {
            objectCache = new ObjectCache();
        } else {
            objectCache = null;
        }
    }

    @Override
    public void setPath(Path path) {
        Path oldPath = this.path;
        this.path = path;
        pcs.firePropertyChange(PROPERTY_ACTIVE_SHEET, oldPath, path);
    }

}
