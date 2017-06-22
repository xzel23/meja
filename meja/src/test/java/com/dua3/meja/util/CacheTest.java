package com.dua3.meja.util;

import org.junit.Ignore;
import org.junit.Test;

import com.dua3.meja.util.Cache.Type;

public class CacheTest {

    @Test @Ignore
    public void testCacheWithWeakKeys() {
        System.out.format("%ntestCacheWithWeakKeys()%n");
        testCacheHelper(Type.WEAK_KEYS);
    }

    @Test @Ignore
    public void testCacheWithStrongKeys() {
        System.out.format("%ntestCacheWithStrongKeys()%n");
        testCacheHelper(Type.STRONG_KEYS);
    }

    public void testCacheHelper(Type type) {
        int chunkSize = 100_000_000; // 100 MB
        int n = 1000; // try to allocate 1000 chunks, totalling 100 GB

        System.out.format("chunk size:  %d%n",      chunkSize);
        System.out.format("number of chunks: %d%n", n);

        try {
            Cache<Object,byte[]> cache = new Cache<>(type, o -> allocate(chunkSize));
            for (int i=1;i<=n; i++) {
                System.out.format("allocating chunk #%d%n", i);
                cache.get(i);
                System.out.format("total memory: %d%n", Runtime.getRuntime().totalMemory());
            }
            System.out.format("Test completed without OOM, cache: %s%n", cache.toString());
        } catch(OutOfMemoryError e) {
            System.out.format("Test failed with OOM.");
        }

    }

    private byte[] allocate(int size) {
        return new byte[size];
    }
}
