package com.dua3.meja.model;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.util.ObjectCache;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * Abstract base class for implementations of the {@link Workbook} interface.
 */
public abstract class AbstractWorkbook implements Workbook {

    /**
     * The path of this workbook.
     */
    private URI uri;

    /**
     * The object cache used to reduce memory usage.
     */
    private ObjectCache objectCache;

    private final SubmissionPublisher<WorkbookEvent> publisher = new SubmissionPublisher<>();

    protected void activeSheetChanged(int idxOld, int idxNew) {
        publisher.submit(new WorkbookEvent.ActiveSheetChanged(this, idxOld, idxNew));
    }

    protected void sheetAdded(int idx) {
        publisher.submit(new WorkbookEvent.SheetAdded(this, idx));
    }

    protected void sheetRemoved(int idx) {
        publisher.submit(new WorkbookEvent.SheetRemoved(this, idx));
    }

    protected void uriChanged(@Nullable URI oldUri, @Nullable URI newUri) {
        publisher.submit(new WorkbookEvent.UriChanged(this, oldUri, newUri));
    }

    protected AbstractWorkbook(@Nullable URI uri) {
        this.uri = uri;
    }

    @Override
    public void subscribe(Flow.Subscriber<WorkbookEvent> subscriber) {
        publisher.subscribe(subscriber);
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
     * @return true, if object caching is enabled
     */
    @Override
    public boolean isObjectCachingEnabled() {
        return objectCache != null;
    }

    /**
     * Enable or disable object caching.
     *
     * @param enabled true, if the object cache should be enabled;
     *                false, if objects should not be cached anymore and the cash cleared
     */
    @Override
    public void setObjectCaching(boolean enabled) {
        objectCache = enabled && !isObjectCachingEnabled() ? new ObjectCache() : null;
    }

    @Override
    public void setUri(URI uri) {
        URI oldUri = this.uri;
        this.uri = uri;
        uriChanged(oldUri, uri);
    }

    @Override
    public void close() throws IOException {
        publisher.close();
    }
}
