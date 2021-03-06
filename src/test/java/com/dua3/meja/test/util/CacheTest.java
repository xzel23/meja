package com.dua3.meja.test.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.dua3.meja.util.Cache;
import com.dua3.meja.util.Cache.Type;

public class CacheTest {

    private static final Logger LOGGER = Logger.getLogger(CacheTest.class.getName());

    @Test
    @Disabled // test is too time consuming
    public void testCacheWithWeakKeys() {
        LOGGER.info("testCacheWithWeakKeys()");
        testCacheHelper(Type.WEAK_KEYS);
    }

    @Test
    @Disabled // test is too time consuming
    public void testCacheWithStrongKeys() {
        LOGGER.info("testCacheWithStrongKeys()");
        testCacheHelper(Type.STRONG_KEYS);
    }

    public void testCacheHelper(Type type) {
        int chunkSize = 10_000_000; // 10 MB
        int n = 1000; // try to allocate 1000 chunks, totalling 10 GB

        LOGGER.log(Level.FINE, "chunk size:  {0}", chunkSize);
        LOGGER.log(Level.FINE, "number of chunks: {0}", n);

        try {
            Cache<Object, byte[]> cache = new Cache<>(type, o -> allocate(chunkSize));
            for (int i = 1; i <= n; i++) {
                LOGGER.log(Level.FINE, "allocating chunk #{0}", i);
                cache.get(i);
                LOGGER.log(Level.FINE, "total memory: {0}", Runtime.getRuntime().totalMemory());
            }
            LOGGER.log(Level.FINE, "Test completed without OOM, cache: {0}", cache);
        } catch (OutOfMemoryError e) {
            LOGGER.severe("Test failed with OOM.");
            fail("Test failed with OOM.");
        }
    }

    private static byte[] allocate(int size) {
        return new byte[size];
    }
}
