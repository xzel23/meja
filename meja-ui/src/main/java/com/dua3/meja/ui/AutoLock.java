package com.dua3.meja.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public class AutoLock implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(AutoLock.class);

    private final Lock lock;
    private final Supplier<String> name;

    public AutoLock(Lock lock, Supplier<String> name) {
        this.lock = lock;
        this.name = name;

        LOG.trace("AutoLock({}): lock [{}]", name::get, () -> System.identityHashCode(this));
        lock.lock();
    }

    @Override
    public void close() {
        LOG.trace("AutoLock({}): unlock [{}]", name::get, () -> System.identityHashCode(this));
        lock.unlock();
    }
}
