package com.dua3.meja.model;

import com.dua3.cabe.annotations.Nullable;

/**
 * Represents a generic event with a source of type T.
 *
 * @param <T> the type of the source object associated with the event
 */
public interface Event<T> {
    /**
     * Returns the source of the event.
     *
     * @return the source of the event
     */
    T source();
    /**
     * Retrieves the type of the event.
     *
     * @return a string representing the type of the event.
     */
    String type();

    /**
     * Abstract base class for events.
     * <p>Represents a generic event with a source and a type.
     *
     * @param <T> the type of the source of the event
     */
    abstract class AbstractEvent<T> implements Event<T> {
        private final T source;
        private final String type;

        /**
         * Constructs a new AbstractEvent instance.
         *
         * @param source the source object where the event originated
         * @param type the type of the event
         */
        protected AbstractEvent(T source, String type) {
            this.source = source;
            this.type = type;
        }

        /**
         * Returns the source of the event.
         *
         * @return the source of the event
         */
        public T source() {
            return source;
        }

        /**
         * Returns the type of the event.
         *
         * @return the event type as a String.
         */
        public String type() {
            return type;
        }
    }

    /**
     * The EventWithIndex class extends AbstractEvent and provides an additional
     * index attribute.
     *
     * @param <T> the type of the source object from which the event originated
     */
    abstract class EventWithIndex<T> extends AbstractEvent<T> {
        private final int idx;

        /**
         * Constructs an EventWithIndex with the specified source, type, and index.
         *
         * @param source the source of the event
         * @param type the type of the event
         * @param idx the index associated with this event
         */
        protected EventWithIndex(T source, String type, int idx) {
            super(source, type);
            this.idx = idx;
        }
        /**
         * Retrieves the index belonging to this event.
         *
         * @return the index belonging to this event
         */
        public int idx() {
            return idx;
        }
    }

    /**
     * Abstract class representing an event where an index value changes.
     *
     * @param <T> the type of the source object associated with the event
     */
    abstract class EventIndexChanged<T> extends AbstractEvent<T> {
        private final int oldIndex;
        private final int newIndex;
        /**
         * Constructs a new EventIndexChanged event.
         *
         * @param source the object on which the Event initially occurred
         * @param type the type of the event
         * @param oldIndex the old index before the change
         * @param newIndex the new index after the change
         */
        protected EventIndexChanged(T source, String type, int oldIndex, int newIndex) {
            super(source, type);
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }
        /**
         * Returns the value of the old index.
         *
         * @return the integer value representing the old index
         */
        public int oldIndex() {
            return oldIndex;
        }
        /**
         * Computes and returns the new index value.
         *
         * @return the computed new index value.
         */
        public int newIndex() {
            return newIndex;
        }
    }

    /**
     * Represents an event where a value change has occurred.
     *
     * @param <T> the type of the event source
     * @param <V> the type of the value that changed
     */
    abstract class EventValueChanged<T,V> extends AbstractEvent<T> {
        private final V oldValue;
        private final V newValue;
        /**
         * Constructor for creating an instance of EventValueChanged.
         *
         * @param source the source of the event
         * @param type the type of the event
         * @param oldValue the old value before the change, can be null
         * @param newValue the new value after the change, can be null
         */
        protected EventValueChanged(T source, String type, @Nullable V oldValue, @Nullable V newValue) {
            super(source, type);
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        /**
         * Returns the old value.
         *
         * @return the previously stored value of type V
         */
        public V oldValue() {
            return oldValue;
        }
        /**
         * Returns the new value of type V.
         *
         * @return the new value of type V
         */
        public V newValue() {
            return newValue;
        }
    }

    /**
     * An abstract class representing an event where a double value has changed.
     *
     * <p>This class extends {@code AbstractEvent} and includes the old and new double values.
     *
     * @param <T> the type of the source object associated with this event
     */
    abstract class EventDoubleValueChanged<T> extends AbstractEvent<T> {
        private final double oldValue;
        private final double newValue;
        /**
         * Constructs a new EventDoubleValueChanged instance representing a change event for a double value.
         *
         * @param source the object on which the event initially occurred
         * @param type the type of the event
         * @param oldValue the old value before the change
         * @param newValue the new value after the change
         */
        protected EventDoubleValueChanged(T source, String type, double oldValue, double newValue) {
            super(source, type);
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        /**
         * Retrieves the old value of the instance.
         *
         * @return the old value as a {@code double}
         */
        public double oldValue() {
            return oldValue;
        }
        /**
         * Retrieves the new value of the instance.
         *
         * @return the new value as a {@code double}
         */
        public double newValue() {
            return newValue;
        }
    }

    /**
     * Represents an abstract event where an integer value has changed.
     *
     * @param <T> the type of the event source
     */
    abstract class EventIntValueChanged<T> extends AbstractEvent<T> {
        private final int oldValue;
        private final int newValue;
        /**
         * Constructs a new EventIntValueChanged.
         *
         * @param source the source of the event
         * @param type the type of the event
         * @param oldValue the old integer value before the change
         * @param newValue the new integer value after the change
         */
        protected EventIntValueChanged(T source, String type, int oldValue, int newValue) {
            super(source, type);
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        /**
         * Retrieves the old value stored in the object.
         *
         * @return the old value as an integer.
         */
        public int oldValue() {
            return oldValue;
        }
        /**
         * Returns the new value.
         *
         * @return the new integer value
         */
        public int newValue() {
            return newValue;
        }
    }
}
