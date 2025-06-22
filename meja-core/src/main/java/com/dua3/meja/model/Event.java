package com.dua3.meja.model;

import org.jspecify.annotations.Nullable;

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

        @Override
        public String toString() {
            return "EventWithIndex{" +
                    "type=" + type() +
                    ", source=" + source() +
                    ", idx=" + idx +
                    '}';
        }
    }

    /**
     * Represents an event where a value change has occurred.
     *
     * @param <T> the type of the event source
     * @param <V> the type of the value that changed
     */
    abstract class EventValueChanged<T, V> extends AbstractEvent<T> {
        private final @Nullable V oldValue;
        private final @Nullable V newValue;

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
        public @Nullable V oldValue() {
            return oldValue;
        }

        /**
         * Returns the new value of type V.
         *
         * @return the new value of type V
         */
        public @Nullable V newValue() {
            return newValue;
        }

        @Override
        public String toString() {
            return "EventValueChanged{" +
                    "type=" + type() +
                    ", source=" + source() +
                    ", oldValue=" + oldValue +
                    ", newValue=" + newValue +
                    '}';
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

        @Override
        public String toString() {
            return "EventIntValueChanged{" +
                    "type=" + type() +
                    ", source=" + source() +
                    ", oldValue=" + oldValue +
                    ", newValue=" + newValue +
                    '}';
        }
    }
}
