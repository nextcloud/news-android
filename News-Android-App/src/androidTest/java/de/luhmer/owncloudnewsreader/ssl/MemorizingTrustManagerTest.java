package de.luhmer.owncloudnewsreader.ssl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;

import javax.crypto.spec.SecretKeySpec;

@RunWith(AndroidJUnit4.class)
public class MemorizingTrustManagerTest {
    private static final String PERSISTED_ALIAS = "persisted-secret";
    private static final char[] KEY_STORE_PASSWORD = "MTM".toCharArray();

    private Context context;
    private File testDirectory;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testDirectory = new File(
                context.getCacheDir(),
                "memorizing-trust-manager-" + System.nanoTime());
        assertTrue(testDirectory.mkdirs());
    }

    @After
    public void tearDown() {
        MemorizingTrustManager.setKeyStoreFile(
                context.getCacheDir().getAbsolutePath(),
                "memorizing-trust-manager-reset-" + System.nanoTime());
        deleteRecursively(testDirectory);
    }

    @Test
    public void readablePersistedKeyStoreIsLoadedWithItsPersistedContent() throws Exception {
        File persistedFile = new File(testDirectory, "valid-keystore");
        KeyStore persisted = KeyStore.getInstance(KeyStore.getDefaultType());
        persisted.load(null, KEY_STORE_PASSWORD);
        persisted.setEntry(
                PERSISTED_ALIAS,
                new SecretKeyEntry(new SecretKeySpec(new byte[] {
                        1, 2, 3, 4, 5, 6, 7, 8,
                        9, 10, 11, 12, 13, 14, 15, 16
                }, "AES")),
                new PasswordProtection(KEY_STORE_PASSWORD));
        try (FileOutputStream output = new FileOutputStream(persistedFile)) {
            persisted.store(output, KEY_STORE_PASSWORD);
        }
        assertTrue(persistedFile.isFile());
        assertTrue(persistedFile.canRead());

        MemorizingTrustManager.setKeyStoreFile(
                testDirectory.getAbsolutePath(), persistedFile.getName());
        MemorizingTrustManager trustManager = new MemorizingTrustManager(context);

        KeyStore loaded = trustManager.loadAppKeyStore();

        assertTrue(loaded.containsAlias(PERSISTED_ALIAS));
        assertTrue(loaded.isKeyEntry(PERSISTED_ALIAS));
        assertNotNull(trustManager.getTrustManager(loaded));
    }

    @Test
    public void absentPersistedKeyStoreReturnsInitializedEmptyFallback() throws Exception {
        File missingFile = new File(testDirectory, "missing-keystore");
        assertFalse(missingFile.exists());
        MemorizingTrustManager.setKeyStoreFile(
                testDirectory.getAbsolutePath(), missingFile.getName());

        MemorizingTrustManager trustManager = new MemorizingTrustManager(context);
        KeyStore loaded = trustManager.loadAppKeyStore();

        assertNotNull(loaded);
        assertEquals(0, loaded.size());
        assertFalse(loaded.aliases().hasMoreElements());
        assertNotNull(trustManager.getTrustManager(loaded));
    }

    @Test
    public void readableMalformedPersistedKeyStoreReturnsInitializedEmptyFallback() throws Exception {
        File malformedFile = new File(testDirectory, "malformed-keystore");
        try (FileOutputStream output = new FileOutputStream(malformedFile)) {
            output.write(new byte[] { 0x01, 0x23, 0x45, 0x67 });
        }
        assertTrue(malformedFile.isFile());
        assertTrue(malformedFile.canRead());
        MemorizingTrustManager.setKeyStoreFile(
                testDirectory.getAbsolutePath(), malformedFile.getName());

        MemorizingTrustManager trustManager = new MemorizingTrustManager(context);
        KeyStore loaded = trustManager.loadAppKeyStore();

        assertNotNull(loaded);
        assertEquals(0, loaded.size());
        assertFalse(loaded.aliases().hasMoreElements());
        assertNotNull(trustManager.getTrustManager(loaded));
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
