package com.dua3.meja.model;

import com.dua3.utility.data.ObjectCache;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * Abstract base class for implementations of the {@link Workbook} interface.
 *
 * @param <S> the type of sheet contained in the workbook
 * @param <R> the type of row contained in the sheets of the workbook
 * @param <C> the type of cell contained in the rows of the sheets in the workbook
 */
public abstract class AbstractWorkbook<S extends AbstractSheet<S, R, C>, R extends AbstractRow<S, R, C>, C extends AbstractCell<S, R, C>> implements Workbook {

    /**
     * The path of this workbook.
     */
    private @Nullable URI uri;

    /**
     * The object cache used to reduce memory usage.
     */
    private @Nullable ObjectCache objectCache;

    private final SubmissionPublisher<WorkbookEvent> publisher = new SubmissionPublisher<>();
    private final Map<Flow.Subscriber<WorkbookEvent>, Flow.Subscription> subscriptions = new ConcurrentHashMap<>();

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

    /**
     * Retrieves the current active sheet in the workbook, or {@code null} if no sheet is active.
     *
     * @return the current active {@link AbstractSheet}, or {@code null} if no sheet is active.
     */
    protected abstract @Nullable S getCurrentAbstractSheetOrNull();

    @Override
    public void subscribe(Flow.Subscriber<WorkbookEvent> subscriber) {
        Flow.Subscriber<WorkbookEvent> wrapper = new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscriptions.put(subscriber, subscription);
                subscriber.onSubscribe(subscription);
            }

            @Override
            public void onNext(WorkbookEvent item) {
                subscriber.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                subscriptions.remove(subscriber);
                subscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                subscriptions.remove(subscriber);
                subscriber.onComplete();
            }
        };
        publisher.subscribe(wrapper);
    }

    @Override
    public void unsubscribe(Flow.Subscriber<WorkbookEvent> subscriber) {
        Flow.Subscription s = subscriptions.remove(subscriber);
        if (s != null) {
            s.cancel();
        }
    }

    @Override
    public final <T> T cache(T obj) {
        return objectCache != null ? objectCache.get(obj) : obj;
    }

    @Override
    public final Optional<URI> getUri() {
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
     * @param enable true, if the object cache should be enabled;
     *                false, if objects should not be cached anymore and the cash cleared
     */
    @Override
    public void setObjectCaching(boolean enable) {
        objectCache = enable && !isObjectCachingEnabled() ? new ObjectCache() : null;
    }

    @Override
    public final void setUri(@Nullable URI uri) {
        URI oldUri = this.uri;
        this.uri = uri;
        uriChanged(oldUri, uri);
    }

    @Override
    public final Optional<Sheet> getCurrentSheet() {
        return Optional.ofNullable(getCurrentAbstractSheetOrNull());
    }

    @Override
    public void close() throws IOException {
        publisher.close();
        subscriptions.clear();
    }
}
