package de.luhmer.owncloudnewsreader.asynctasks

import de.luhmer.owncloudnewsreader.async_tasks.RssItemToHtmlTask
import org.junit.Assert.assertEquals
import org.junit.Test

class RssItemToHtmlTaskTest {
    @Test
    fun moreThan10PreBlocks() {
        val input = (1..11).joinToString(" ") { "<pre>$it</pre>" }
        val result = RssItemToHtmlTask.removeLineBreaksFromHtml(input)
        assertEquals(input, result)
    }

    @Test
    fun preBlockWithRegexReplacementChars() {
        val input = "<pre>$$$</pre>"
        val result = RssItemToHtmlTask.removeLineBreaksFromHtml(input)
        assertEquals(input, result)
    }

    @Test
    fun repeatedPreBlocks() {
        val input = (1..11).joinToString(" ") { "<pre>1</pre>" }
        val result = RssItemToHtmlTask.removeLineBreaksFromHtml(input)
        assertEquals(input, result)
    }
}
