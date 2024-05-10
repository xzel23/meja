package com.dua3.meja.model;

import com.dua3.cabe.annotations.Nullable;

public interface Event<T> {
    public T source();
    public String type();

    abstract class AbstractEvent<T> implements Event<T> {
        private final T source;
        private final String type;

        protected AbstractEvent(T source, String type) {
            this.source = source;
            this.type = type;
        }

        public T source() {
            return source;
        }

        public String type() {
            return type;
        }
    }

    abstract class EventWithIndex<T> extends AbstractEvent<T> {
        private final int idx;
        protected EventWithIndex(T source, String type, int idx) {
            super(source, type);
            this.idx = idx;
        }
        public int idx() {
            return idx;
        }
    }

    abstract class EventIndexChanged<T> extends AbstractEvent<T> {
        private final int idxOld;
        private final int idxNew;
        protected EventIndexChanged(T source, String type, int idxOld, int idxNew) {
            super(source, type);
            this.idxOld = idxOld;
            this.idxNew = idxNew;
        }
        public int idxOld() {
            return idxOld;
        }
        public int getIdxNew() {
            return idxNew;
        }
    }

    abstract class EventValueChanged<T,V> extends AbstractEvent<T> {
        private final V valueOld;
        private final V valueNew;
        protected EventValueChanged(T source, String type, @Nullable V valueOld, @Nullable V valueNew) {
            super(source, type);
            this.valueOld = valueOld;
            this.valueNew = valueNew;
        }
        public V valueOld() {
            return valueOld;
        }
        public V valueNew() {
            return valueNew;
        }
    }

    abstract class EventDoubleValueChanged<T> extends AbstractEvent<T> {
        private final double valueOld;
        private final double valueNew;
        protected EventDoubleValueChanged(T source, String type, double valueOld, double valueNew) {
            super(source, type);
            this.valueOld = valueOld;
            this.valueNew = valueNew;
        }
        public double valueOld() {
            return valueOld;
        }
        public double getvalueNew() {
            return valueNew;
        }
    }

    abstract class EventIntValueChanged<T> extends AbstractEvent<T> {
        private final int valueOld;
        private final int valueNew;
        protected EventIntValueChanged(T source, String type, int valueOld, int valueNew) {
            super(source, type);
            this.valueOld = valueOld;
            this.valueNew = valueNew;
        }
        public int valueOld() {
            return valueOld;
        }
        public int getvalueNew() {
            return valueNew;
        }
    }
}
