package com.dua3.meja.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;

import com.dua3.meja.util.ObjectCache;

public abstract class AbstractWorkbook
        implements Workbook {

    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
    *
    */
    protected URI uri;

    /**
     * The object cache used to reduce memory usage.
     */
    private ObjectCache objectCache = null;

    protected final Locale locale;

    public AbstractWorkbook(Locale locale, URI uri) {
        this.locale = locale;
        this.uri = uri;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Get cached instance of object.
     *
     * @param <T>
     *            object type
     * @param obj
     *            the object to lookup
     * @return the cached instance, if an instance equal to {@code obj} is
     *         present in the cache, {@code obj} otherwise
     *
     */
    public <T> T cache(T obj) {
        return objectCache != null ? objectCache.get(obj) : obj;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Optional<URI> getUri() {
        return Optional.ofNullable(uri);
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
    public void setUri(URI uri) {
        URI oldUri = this.uri;
        this.uri = uri;
        pcs.firePropertyChange(PROPERTY_ACTIVE_SHEET, oldUri, uri);
    }

}
