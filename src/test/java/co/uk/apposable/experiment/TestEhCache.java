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
    public void testLimitedElements() throws InterruptedException {
        // Configure and create cache
        CacheManager cm = CacheManager.getInstance();
        CacheConfiguration config = new CacheConfiguration();
        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
        config.setName("test-cache-with-max-elements");
        config.setMaxEntriesLocalHeap(10);
        config.setMaxEntriesLocalDisk(20);
        Cache cache = new Cache(config);
        cm.addCache(cache);

        // Fill cache
        fillCache(cache);

        sleep();

        int cacheSize = cache.getSize();
        File cacheFile = new File(System.getProperty("java.io.tmpdir"), cache.getName() + ".data");
        long onDiskLength = cacheFile.length();

        assertTrue("Cache size should be less than or equal to 20, but is " + cacheSize, cacheSize <= 20);
        System.out.println("On disk size in bytes = " + onDiskLength);

        cm.shutdown();
    }

    @Test
    public void testLimitedBytes() throws InterruptedException {
        // Configure and create cache
        CacheManager cm = CacheManager.getInstance();
        CacheConfiguration config = new CacheConfiguration();
        config.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
        config.setName("test-cache-with-max-bytes");
        // Allow more entries than we are going to use.
        config.setMaxEntriesLocalHeap(2000);
        config.setMaxEntriesLocalDisk(2000);
        config.setMaxBytesLocalDisk(6000L);
        Cache cache = new Cache(config);
        cm.addCache(cache);

        // Fill cache
        fillCache(cache);

        // Give ehcache time to catch up
        sleep();

        int cacheSize = cache.getSize();
        File cacheFile = new File(System.getProperty("java.io.tmpdir"), cache.getName() + ".data");
        long onDiskLength = cacheFile.length();

        System.out.println("Num elements in cache = " + cacheSize);
        assertTrue("On disk size in bytes should be less than or equal to 6000, but is " + onDiskLength, onDiskLength <= 6000);

        cm.shutdown();
    }

    private void fillCache(Cache cache) {
        for (int i = 0; i < 1000; i++) {
            cache.put(new Element(i, "Object " + i));
        }
    }
    
    private void sleep() throws InterruptedException {
        Thread.sleep(10 * 1000);
    }
}
