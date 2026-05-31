package de.luhmer.owncloudnewsreader.junit_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

import de.luhmer.owncloudnewsreader.services.podcast.TtsTextSplitter;

public class TtsTextSplitterTest {

    @Test
    public void testNullReturnsEmpty() {
        assertTrue(TtsTextSplitter.split(null, 100).isEmpty());
    }

    @Test
    public void testEmptyReturnsEmpty() {
        assertTrue(TtsTextSplitter.split("   ", 100).isEmpty());
    }

    @Test
    public void testShortTextStaysSingleChunk() {
        List<String> chunks = TtsTextSplitter.split("Hello world.", 100);
        assertEquals(1, chunks.size());
        assertEquals("Hello world.", chunks.get(0));
    }

    @Test
    public void testLongTextIsSplitAndEachChunkWithinLimit() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("This is sentence number ").append(i).append(". ");
        }
        int maxLen = 200;
        List<String> chunks = TtsTextSplitter.split(sb.toString(), maxLen);
        assertTrue("expected multiple chunks", chunks.size() > 1);
        for (String chunk : chunks) {
            assertTrue("chunk exceeds maxLen: " + chunk.length(), chunk.length() <= maxLen);
        }
    }

    @Test
    public void testSplitsAtSentenceBoundary() {
        // Two sentences, limit forces a split; first chunk should end at the sentence end.
        String text = "First sentence is here. Second sentence is here too.";
        List<String> chunks = TtsTextSplitter.split(text, 30);
        assertEquals("First sentence is here.", chunks.get(0));
    }

    @Test
    public void testFallsBackToWhitespaceWhenNoSentenceBoundary() {
        String text = "alpha beta gamma delta epsilon zeta eta theta";
        int maxLen = 20;
        List<String> chunks = TtsTextSplitter.split(text, maxLen);
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= maxLen);
            assertTrue("word was cut in half: '" + chunk + "'",
                    !chunk.startsWith(" ") && !chunk.endsWith(" "));
        }
        // No data lost: re-joining the words yields the original sequence.
        assertEquals(text, String.join(" ", chunks));
    }

    @Test
    public void testHardCutForSingleVeryLongWord() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append('x');
        }
        int maxLen = 10;
        List<String> chunks = TtsTextSplitter.split(sb.toString(), maxLen);
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= maxLen);
        }
        // No characters lost.
        assertEquals(50, String.join("", chunks).length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMaxLenThrows() {
        TtsTextSplitter.split("text", 0);
    }
}
