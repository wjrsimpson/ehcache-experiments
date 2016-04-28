package co.uk.apposable.experiment;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;

/**
 * 
 * @author Will Simpson
 *
 */
public class TestEhCache {

    @Test
    public void testLimitedEntries() throws InterruptedException {
        // Configure and create cache
        CacheManager cm = CacheManager.getInstance();
        CacheConfiguration config = new CacheConfiguration();
        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
        config.setName("test-cache-with-max-entries");
        config.setMaxEntriesLocalHeap(10);
        config.setMaxEntriesLocalDisk(20);
        Cache cache = new Cache(config);
        cm.addCache(cache);

        // Fill cache
        long totalSerializedSize = fillCache(cache);

        sleep();

        int cacheSize = cache.getSize();
        File cacheFile = new File(System.getProperty("java.io.tmpdir"), cache.getName() + ".data");
        long onDiskLength = cacheFile.length();
        System.out.println("Num elements in cache = " + cacheSize);
        System.out.println("On disk size in bytes = " + onDiskLength);

        assertTrue("Cache size should be less than or equal to 20, but is " + cacheSize, cacheSize <= 20);
        long maxBytesLocalDisk = 6000L;
        assertTrue("On disk size in bytes should be less than or equal to " + maxBytesLocalDisk + ", but is " + onDiskLength + " (total serialized size is "
                + totalSerializedSize + ")", onDiskLength <= (maxBytesLocalDisk * 1.15));

        cm.shutdown();
    }

    @Test
    public void testLimitedDiskBytesWithSmallerHeapEntries() throws InterruptedException {
        // Configure and create cache
        CacheManager cm = CacheManager.getInstance();
        CacheConfiguration config = new CacheConfiguration();
        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
        config.setName("test-cache-with-max-bytes-with-smaller-heap-entries");
        // 18 entries will be slightly less than 6000 bytes
        config.setMaxEntriesLocalHeap(15);
        long maxBytesLocalDisk = 6000L;
        config.setMaxBytesLocalDisk(maxBytesLocalDisk);
        Cache cache = new Cache(config);
        cm.addCache(cache);

        // Fill cache
        long totalSerializedSize = fillCache(cache);

        // Give ehcache time to catch up
        sleep();

        int cacheSize = cache.getSize();
        File cacheFile = new File(System.getProperty("java.io.tmpdir"), cache.getName() + ".data");
        long onDiskLength = cacheFile.length();
        System.out.println("Num elements in cache = " + cacheSize);
        System.out.println("On disk size in bytes = " + onDiskLength);

        assertTrue("On disk size in bytes should be less than or equal to " + maxBytesLocalDisk + ", but is " + onDiskLength + " (total serialized size is "
                + totalSerializedSize + ")", onDiskLength <= (maxBytesLocalDisk * 1.15));
        assertTrue("Expected cache size to be greater than 15, but was " + cacheSize, cacheSize > 15);

        cm.shutdown();
    }

    @Test
    public void testLimitedDiskBytesWithLargerHeapEntries() throws InterruptedException {
        // Configure and create cache
        CacheManager cm = CacheManager.getInstance();
        CacheConfiguration config = new CacheConfiguration();
        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
        config.setName("test-cache-with-max-bytes-with-larger-heap-entries");
        // 19 entries will be slightly more than 6000 bytes
        config.setMaxEntriesLocalHeap(19);
        long maxBytesLocalDisk = 6000L;
        config.setMaxBytesLocalDisk(maxBytesLocalDisk);
        Cache cache = new Cache(config);
        cm.addCache(cache);

        // Fill cache
        long totalSerializedSize = fillCache(cache);

        // Give ehcache time to catch up
        sleep();

        int cacheSize = cache.getSize();
        File cacheFile = new File(System.getProperty("java.io.tmpdir"), cache.getName() + ".data");
        long onDiskLength = cacheFile.length();
        System.out.println("Num elements in cache = " + cacheSize);
        System.out.println("On disk size in bytes = " + onDiskLength);

        assertTrue("On disk size in bytes should be less than or equal to " + maxBytesLocalDisk + ", but is " + onDiskLength + " (total serialized size is "
                + totalSerializedSize + ")", onDiskLength <= (maxBytesLocalDisk * 1.15));

        cm.shutdown();
    }

    @Test
    public void testLimitedBytesOnHeapWithNoDisk() throws InterruptedException {
        // Configure and create cache
        CacheManager cm = CacheManager.getInstance();
        CacheConfiguration config = new CacheConfiguration();
        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE));
        config.setName("test-cache-with-max-bytes-on-heap");
        config.setMaxBytesLocalHeap(6000L);
        Cache cache = new Cache(config);
        cm.addCache(cache);

        // Fill cache
        fillCache(cache);

        // Give ehcache time to catch up
        sleep();

        int cacheSize = cache.getSize();
        System.out.println("Num elements in cache = " + cacheSize);

        assertTrue("Expected cache size to be <= 30, but is " + cacheSize, cacheSize <= 30);
        cm.shutdown();
    }

    @Test
    public void testLimitedDiskBytesWithHeapSmallerThanDisk() throws InterruptedException {
        // Configure and create cache
        CacheManager cm = CacheManager.getInstance();
        CacheConfiguration config = new CacheConfiguration();
        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
        config.setName("test-cache-with-max-bytes-with-heap-smaller-than-disk");
        // 3000 bytes should allow 9 entries
        config.setMaxBytesLocalHeap(3000L);
        // 6000 bytes should allow 18 entries
        long maxBytesLocalDisk = 6000L;
        config.setMaxBytesLocalDisk(maxBytesLocalDisk);
        Cache cache = new Cache(config);
        cm.addCache(cache);

        // Fill cache
        long totalSerializedSize = fillCache(cache);

        // Give ehcache time to catch up
        sleep();

        int cacheSize = cache.getSize();
        File cacheFile = new File(System.getProperty("java.io.tmpdir"), cache.getName() + ".data");
        long onDiskLength = cacheFile.length();
        System.out.println("Num elements in cache = " + cacheSize);
        System.out.println("On disk size in bytes = " + onDiskLength);

        assertTrue("On disk size in bytes should be less than or equal to " + maxBytesLocalDisk + ", but is " + onDiskLength + " (total serialized size is "
                + totalSerializedSize + ")", onDiskLength <= (maxBytesLocalDisk * 1.15));
        // assertTrue("Expected cache size to be greater than 15, but was " +
        // cacheSize, cacheSize > 15);

        cm.shutdown();
    }

    @Test
    public void testLimitedDiskBytesWithHeapLargerThanDisk() throws InterruptedException {
        // Configure and create cache
        CacheManager cm = CacheManager.getInstance();
        CacheConfiguration config = new CacheConfiguration();
        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
        config.setName("test-cache-with-max-bytes-with-heap-larger-than-disk");
        // Allow more entries than we are going to use.
        config.setMaxBytesLocalHeap(9000L);
        long maxBytesLocalDisk = 6000L;
        config.setMaxBytesLocalDisk(maxBytesLocalDisk);
        Cache cache = new Cache(config);
        cm.addCache(cache);

        // Fill cache
        long totalSerializedSize = fillCache(cache);

        // Give ehcache time to catch up
        sleep();

        int cacheSize = cache.getSize();
        File cacheFile = new File(System.getProperty("java.io.tmpdir"), cache.getName() + ".data");
        long onDiskLength = cacheFile.length();
        System.out.println("Num elements in cache = " + cacheSize);
        System.out.println("On disk size in bytes = " + onDiskLength);

        assertTrue("On disk size in bytes should be less than or equal to " + maxBytesLocalDisk + ", but is " + onDiskLength + " (total serialized size is "
                + totalSerializedSize + ")", onDiskLength <= (maxBytesLocalDisk * 1.15));

        cm.shutdown();
    }

    private long fillCache(Cache cache) {
        long totalSerializedSize = 0;
        for (int i = 0; i < 1000; i++) {
            // Each element will be 317 bytes
            Element element = new Element(i, "Object " + String.format("%03d", i));
            long serializedSize = element.getSerializedSize();
            totalSerializedSize += serializedSize;
            cache.put(element);
        }
        return totalSerializedSize;
    }

    private void sleep() throws InterruptedException {
        Thread.sleep(30 * 1000);
    }
}
