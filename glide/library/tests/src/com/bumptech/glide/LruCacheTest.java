package com.bumptech.glide;

import android.content.ComponentCallbacks2;
import android.graphics.Bitmap;
import android.test.AndroidTestCase;
import com.bumptech.glide.resize.cache.LruMemoryCache;
import com.bumptech.glide.resize.cache.MemoryCache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class LruCacheTest extends AndroidTestCase {
    // 1MB
    private static final int SIZE = 1024 * 1024;
    private LruMemoryCache cache;
    private String key;
    private Bitmap test;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cache = new LruMemoryCache(SIZE);
        key = getKey();
        test = getBitmap();
    }

    public void testCanAddABitmap() {
        cache.put(key, test);
        assertNotNull(cache.get(key));
    }

    public void testLeastRecentlyAddKeyEvictedFirstIfGetsAreEqual() {
        LruMemoryCache smallCache = new LruMemoryCache(2 * getSize(test));
        smallCache.put(key, test);
        smallCache.put(getKey(), getBitmap());
        final AtomicInteger totalRemoved = new AtomicInteger();
        smallCache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                assertEquals(test, removed);
                totalRemoved.getAndIncrement();
            }
        });
        smallCache.put(getKey(), getBitmap());
        assertEquals(1, totalRemoved.get());
    }

    public void testLeastRecentlyUsedKeyEvictedFirst() {
        LruMemoryCache smallCache = new LruMemoryCache(2 * getSize(test));
        smallCache.put(key, test);
        smallCache.put(getKey(), getBitmap());
        smallCache.get(key);
        final AtomicInteger totalRemoved = new AtomicInteger();
        smallCache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                assertNotSame(test, removed);
                totalRemoved.getAndIncrement();
            }
        });
        smallCache.put(getKey(), getBitmap());
        assertEquals(1, totalRemoved.get());
    }

    public void testImageRemovedListenerCalledWhenBitmapEvicted() {
        LruMemoryCache smallCache = new LruMemoryCache(getSize(test));
        smallCache.put(getKey(), test);
        final AtomicInteger totalRemoved = new AtomicInteger();
        smallCache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                totalRemoved.getAndIncrement();
            }
        });
        smallCache.put(getKey(), getBitmap());
        assertEquals(1, totalRemoved.get());
    }

    public void testBitmapLargerThanCacheIsImmediatelyEvicted() {
        final Bitmap tooLarge = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        assertTrue(getSize(tooLarge) > SIZE);
        final AtomicInteger totalRemoved = new AtomicInteger();
        cache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                totalRemoved.incrementAndGet();
                assertEquals(tooLarge, removed);
            }
        });
        cache.put(key, tooLarge);
        assertFalse(cache.contains(key));
        assertEquals(1, totalRemoved.get());
    }

    public void testEmptyCacheDoesNotContainKey() {
        assertFalse(cache.contains(key));
    }

    public void testCacheContainsAddedBitmap() {
        assertFalse(cache.contains(key));
        cache.put(key, test);
        assertTrue(cache.contains(key));
    }

    public void testItIsSizeLimited() {
        final AtomicInteger totalRemoved = new AtomicInteger();
        cache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                totalRemoved.incrementAndGet();
            }
        });
        fillCache();
        assertEquals(0, totalRemoved.get());
        cache.put(key, test);
        assertEquals(1, totalRemoved.get());
    }

    public void testClearMemoryDoesNotRecycleBitmaps() {
        fillCache();
        final AtomicInteger cleared = new AtomicInteger();
        cache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                assertFalse(removed.isRecycled());
                cleared.getAndIncrement();
            }
        });
        cache.clearMemory();
        assertTrue(cleared.get() > 0);
    }

    public void testTrimMemoryDoesNotRecycleBitmaps() {
        fillCache();
        final AtomicInteger cleared = new AtomicInteger();
        cache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                assertFalse(removed.isRecycled());
                cleared.getAndIncrement();
            }
        });
        cache.trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
        assertTrue(cleared.get() > 0);
    }

    public void testClearMemoryCallsListener() {
        List<String> keys = fillCache();
        final AtomicInteger totalRemoved = new AtomicInteger();
        cache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                totalRemoved.getAndIncrement();
            }
        });
        cache.clearMemory();
        assertEquals(keys.size(), totalRemoved.get());
    }

    public void testTrimMemoryCallsListener() {
        List<String> keys = fillCache();
        final AtomicInteger totalRemoved = new AtomicInteger();
        cache.setImageRemovedListener(new MemoryCache.ImageRemovedListener() {
            @Override
            public void onImageRemoved(Bitmap removed) {
                totalRemoved.getAndIncrement();
            }
        });
        cache.trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
        assertEquals(keys.size(), totalRemoved.get());
    }

    public void testClearMemory() {
        List<String> keys = fillCache();
        cache.clearMemory();
        for (String key : keys) {
            assertFalse(cache.contains(key));
        }
    }

    public void testTrimMemoryCompleteClearsCache() {
        List<String> keys = fillCache();
        cache.trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
        for (String key : keys) {
            assertFalse(cache.contains(key));
        }
    }

    public void testTrimMemoryModerateClearsCache() {
        List<String> keys = fillCache();
        cache.trimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);
        for (String key : keys) {
            assertFalse(cache.contains(key));
        }
    }

    public void testTrimMemoryBackgroundRemovesHalfOfCache() {
        List<String> keys = fillCache();
        cache.trimMemory(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND);
        int totalMisses = 0;
        for (String key : keys) {
            if (!cache.contains(key)) {
                totalMisses++;
            }
        }

        assertEquals((int) Math.ceil(keys.size() / (double) 2), totalMisses);
    }

    private List<String> fillCache() {
        List<String> keys = new ArrayList<String>();
        Bitmap toPut = getBitmap();
        int bitmapSize = getSize(toPut);
        for (int i = 0; i < SIZE / bitmapSize; i++) {
            final String key = getKey();
            cache.put(key, Bitmap.createBitmap(toPut));
            keys.add(key);
        }

        for (String key : keys) {
            assertTrue(cache.contains(key));
        }

        return keys;
    }

    private static Bitmap getBitmap() {
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    }

    private static int getSize(Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    private static String getKey() {
        return UUID.randomUUID().toString();
    }
}
