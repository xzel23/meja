package com.dua3.meja.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.Optional;

import com.dua3.meja.util.ObjectCache;

public abstract class AbstractWorkbook implements Workbook {

    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * The path of this workbook.
     */
    protected URI uri;

    /**
     * The object cache used to reduce memory usage.
     */
    private ObjectCache objectCache = null;

    protected AbstractWorkbook(URI uri) {
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

    @Override
    public <T> T cache(T obj) {
        return objectCache != null ? objectCache.get(obj) : obj;
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
     * @param enabled true, if the object cache should be enabled false, if objects
     *                should not be cached anymore and the cash cleared
     */
    @Override
    public void setObjectCaching(boolean enabled) {
        objectCache = enabled && !isObjectCachingEnabled() ? new ObjectCache() : null;
    }

    @Override
    public void setUri(URI uri) {
        URI oldUri = this.uri;
        this.uri = uri;
        firePropertyChange(PROPERTY_ACTIVE_SHEET, oldUri, this.uri);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

}
