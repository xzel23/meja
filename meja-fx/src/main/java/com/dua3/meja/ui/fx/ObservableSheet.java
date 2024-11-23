package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.SheetEvent;
import com.dua3.meja.model.SheetEvent.RowsAdded;
import javafx.collections.ObservableListBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.Flow;

/**
 * ObservableSheet is a wrapper around a Sheet that implements an observable list of Rows.
 * It extends {@code ObservableListBase<Row>} to provide the necessary observable list functionalities.
 * Internally, it subscribes to sheet events and updates the observable list accordingly.
 */
public class ObservableSheet extends ObservableListBase<Row> {
    private static final Logger LOG = LogManager.getLogger(ObservableSheet.class);

    private final Sheet sheet;

    /**
     * Constructs an ObservableSheet that wraps a given {@code Sheet}.
     * This constructor subscribes an internal {@code SheetTracker} to the sheet
     * to monitor sheet events and update the observable list dynamically.
     *
     * @param sheet the sheet to wrap and observe
     */
    public ObservableSheet(Sheet sheet) {
        this.sheet = sheet;
        this.sheet.subscribe(new SheetTracker());
    }

    @Override
    public Row get(int index) {
        return sheet.getRow(index);
    }

    @Override
    public int size() {
        return sheet.getRowCount();
    }

    private class SheetTracker implements Flow.Subscriber<SheetEvent> {

        private Flow.@Nullable Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(SheetEvent item) {
            if (item instanceof RowsAdded rowsAdded) {
                beginChange();
                nextAdd(rowsAdded.first(), rowsAdded.last());
                endChange();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.warn("error tracking sheet", throwable);
        }

        @Override
        public void onComplete() {
            LOG.warn("tracking sheet completed");
        }
    }
}