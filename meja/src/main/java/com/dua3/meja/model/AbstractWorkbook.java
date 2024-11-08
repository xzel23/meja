package com.dua3.meja.model;

import com.dua3.meja.util.ObjectCache;
import org.jspecify.annotations.Nullable;

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
    private @Nullable URI uri;

    /**
     * The object cache used to reduce memory usage.
     */
    private @Nullable ObjectCache objectCache;

    private final SubmissionPublisher<WorkbookEvent> publisher = new SubmissionPublisher<>();

    /**
     * Notifies subscribers that the active sheet of the workbook has changed.
     *
     * @param idxOld the index of the old active sheet.
     * @param idxNew the index of the new active sheet.
     */
    protected void activeSheetChanged(int idxOld, int idxNew) {
        publisher.submit(new WorkbookEvent.ActiveSheetChanged(this, idxOld, idxNew));
    }

    /**
     * Notifies subscribers that a sheet has been added to the workbook.
     *
     * @param idx the index of the newly added sheet
     */
    protected void sheetAdded(int idx) {
        publisher.submit(new WorkbookEvent.SheetAdded(this, idx));
    }

    /**
     * Notifies the subscribers that a sheet has been removed from the workbook.
     *
     * @param idx the index of the removed sheet
     */
    protected void sheetRemoved(int idx) {
        publisher.submit(new WorkbookEvent.SheetRemoved(this, idx));
    }

    /**
     * Notifies subscribers that the URI of the workbook has changed.
     *
     * @param oldUri The old URI of the workbook.
     * @param newUri The new URI of the workbook.
     */
    protected void uriChanged(@Nullable URI oldUri, @Nullable URI newUri) {
        publisher.submit(new WorkbookEvent.UriChanged(this, oldUri, newUri));
    }

    /**
     * Constructs an instance of the {@code AbstractWorkbook} class.
     *
     * @param uri the URI of the workbook
     */
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
    public void setUri(@Nullable URI uri) {
        URI oldUri = this.uri;
        this.uri = uri;
        uriChanged(oldUri, uri);
    }

    @Override
    public void close() throws IOException {
        publisher.close();
    }
}
