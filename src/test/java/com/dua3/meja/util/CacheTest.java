package com.dua3.meja.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.meja.util.Cache.Type;

public class CacheTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheTest.class);

    @Test
    public void testCacheWithWeakKeys() {
        LOGGER.info("testCacheWithWeakKeys()");
        testCacheHelper(Type.WEAK_KEYS);
    }

    @Test
    public void testCacheWithStrongKeys() {
        LOGGER.info("testCacheWithStrongKeys()");
        testCacheHelper(Type.STRONG_KEYS);
    }

    public void testCacheHelper(Type type) {
        int chunkSize = 10_000_000; // 10 MB
        int n = 1000; // try to allocate 1000 chunks, totalling 10 GB

        LOGGER.debug("chunk size:  {}", chunkSize);
        LOGGER.debug("number of chunks: {}", n);

        try {
            Cache<Object,byte[]> cache = new Cache<>(type, o -> allocate(chunkSize));
            for (int i=1;i<=n; i++) {
                LOGGER.debug("allocating chunk #{}", i);
                cache.get(i);
                LOGGER.debug("total memory: {}", Runtime.getRuntime().totalMemory());
            }
            LOGGER.debug("Test completed without OOM, cache: {}", cache);
        } catch(OutOfMemoryError e) {
            LOGGER.error("Test failed with OOM.");
            Assert.fail("Test failed with OOM.");
        }
    }

    private static byte[] allocate(int size) {
        return new byte[size];
    }
}
