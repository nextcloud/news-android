package de.luhmer.owncloudnewsreader.junit_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import de.luhmer.owncloudnewsreader.helper.PodcastPositionStore;

@RunWith(RobolectricTestRunner.class)
public class PodcastPositionStoreTest {

    private static final String FINGERPRINT = "abc-123";

    private PodcastPositionStore store;

    @Before
    public void setUp() {
        store = new PodcastPositionStore(RuntimeEnvironment.getApplication());
        store.clearAll();
    }

    @Test
    public void testSaveAndRestore() {
        store.savePosition(42L, FINGERPRINT, 90_000, 3_600_000);
        assertEquals(90_000, store.getPosition(42L, FINGERPRINT));
    }

    @Test
    public void testMissingEntryReturnsZero() {
        assertEquals(0, store.getPosition(42L, FINGERPRINT));
    }

    @Test
    public void testFingerprintMismatchInvalidatesPosition() {
        store.savePosition(42L, FINGERPRINT, 90_000, 3_600_000);
        assertEquals(0, store.getPosition(42L, "other-fingerprint"));
    }

    @Test
    public void testImplausiblePositionReturnsZero() {
        // position beyond the stored duration must be ignored
        store.savePosition(42L, FINGERPRINT, 3_700_000, 3_600_000);
        assertEquals(0, store.getPosition(42L, FINGERPRINT));
    }

    @Test
    public void testClearPosition() {
        store.savePosition(42L, FINGERPRINT, 90_000, 3_600_000);
        store.clearPosition(42L);
        assertEquals(0, store.getPosition(42L, FINGERPRINT));
    }

    @Test
    public void testRemovePositions() {
        store.savePosition(1L, FINGERPRINT, 90_000, 3_600_000);
        store.savePosition(2L, FINGERPRINT, 90_000, 3_600_000);
        assertEquals(2, store.getStoredItemIds().size());

        store.removePositions(store.getStoredItemIds());
        assertTrue(store.getStoredItemIds().isEmpty());
        assertEquals(0, store.getPosition(1L, FINGERPRINT));
    }
}
