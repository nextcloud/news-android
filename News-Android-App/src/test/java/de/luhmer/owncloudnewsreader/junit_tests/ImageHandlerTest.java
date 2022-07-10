package de.luhmer.owncloudnewsreader.junit_tests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import de.luhmer.owncloudnewsreader.helper.ImageHandler;

public class ImageHandlerTest {

    @Test
    public void testHref_CASE_MISSING_PROTOCOL() {
        String articleUrl = "https://www.reddit.com/";
        String content =
                "<p><a rel=\"noreferrer\" href=\"//abc.de\">Test</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"//abcd.de\">Test</a></p>";
        String expectedResult =
                "<p><a rel=\"noreferrer\" href=\"https://abc.de\">Test</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"https://abcd.de\">Test</a></p>";
        String result = ImageHandler.fixBrokenHrefInArticle(articleUrl, content);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testHref_CASE_ABSOLUTE_URL() {
        String articleUrl = "https://www.reddit.com/r/MsMarvelShow/comments/vp6qrp/continuing_the_discussion/";
        String content =
                "<p><a rel=\"noreferrer\" href=\"/r/LokiTV\">r/LokiTV</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"/r/shehulk\">r/shehulk</a></p>";
        String expectedResult =
                "<p><a rel=\"noreferrer\" href=\"https://www.reddit.com/r/LokiTV\">r/LokiTV</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"https://www.reddit.com/r/shehulk\">r/shehulk</a></p>";
        String result = ImageHandler.fixBrokenHrefInArticle(articleUrl, content);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testHref_CASE_RELATIVE_FILE_END() {
        String articleUrl = "https://www.reddit.com/subdir";
        String content =
                "<p><a rel=\"noreferrer\" href=\"articles/matrix-vs-xmpp.html\">Test</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"articles/matrix-vs-xmpp2.html\">Test</a></p>";
        String expectedResult =
                "<p><a rel=\"noreferrer\" href=\"https://www.reddit.com/articles/matrix-vs-xmpp.html\">Test</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"https://www.reddit.com/articles/matrix-vs-xmpp2.html\">Test</a></p>";
        String result = ImageHandler.fixBrokenHrefInArticle(articleUrl, content);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testHref_CASE_RELATIVE_PARENT() {
        String articleUrl = "https://www.reddit.com/subdir";
        String content =
                "<p><a rel=\"noreferrer\" href=\"../articles/matrix-vs-xmpp.html\">Test</a></p>"+
                "<p><a rel=\"noreferrer\" href=\"../articles/matrix-vs-xmpp.html2\">Test</a></p>";
        String expectedResult =
                "<p><a rel=\"noreferrer\" href=\"https://www.reddit.com/articles/matrix-vs-xmpp.html\">Test</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"https://www.reddit.com/articles/matrix-vs-xmpp.html2\">Test</a></p>";
        String result = ImageHandler.fixBrokenHrefInArticle(articleUrl, content);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testHref_CASE_RELATIVE_ADD_HOST() {
        String articleUrl = "https://www.reddit.com/subdir/";
        String content =
                "<p><a rel=\"noreferrer\" href=\"subsubdir/articles.html\">Test</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"subsubdir/articles2.html\">Test</a></p>";
        String expectedResult =
                "<p><a rel=\"noreferrer\" href=\"https://www.reddit.com/subdir/subsubdir/articles.html\">Test</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"https://www.reddit.com/subdir/subsubdir/articles2.html\">Test</a></p>";
        String result = ImageHandler.fixBrokenHrefInArticle(articleUrl, content);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testHref_CASE_RELATIVE_DOMAIN_OR_FILE() {
        String articleUrl = "https://sscpodcast.libsyn.com/eight-hundred-slightly-poisoned-word-games";
        String content =
                "<p><a rel=\"noreferrer\" href=\"astralcodexten.substack.com\">astralcodexten.substack.com</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"astralcodexten.substack2.com\">astralcodexten.substack2.com</a></p>";
        String expectedResult =
                "<p><a rel=\"noreferrer\" href=\"astralcodexten.substack.com\">astralcodexten.substack.com</a></p>" +
                "<p><a rel=\"noreferrer\" href=\"astralcodexten.substack2.com\">astralcodexten.substack2.com</a></p>";
        String result = ImageHandler.fixBrokenHrefInArticle(articleUrl, content);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testHref_CASE_RELATIVE_DOMAIN_SUBPATH() {
        String articleUrl = "https://sscpodcast.libsyn.com/model-city-monday";
        String content =
                "<p><a rel=\"noreferrer\" href=\"astralcodexten.substack.com/subscribe\">astralcodexten.substack.com/subscribe</a></p>" +
                        "<p><a rel=\"noreferrer\" href=\"astralcodexten.substack2.com/subscribe\">astralcodexten.substack2.com/subscribe</a></p>";
        String expectedResult =
                "<p><a rel=\"noreferrer\" href=\"astralcodexten.substack.com/subscribe\">astralcodexten.substack.com/subscribe</a></p>"+
                "<p><a rel=\"noreferrer\" href=\"astralcodexten.substack2.com/subscribe\">astralcodexten.substack2.com/subscribe</a></p>";
        String result = ImageHandler.fixBrokenHrefInArticle(articleUrl, content);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testImg_CASE_MISSING_PROTOCOL() {
        String articleUrl = "http://blog.cleancoder.com/uncle-bob/2021/03/06/ifElseSwitch.html";
        String content =
                "<p><img src=\"//blog.cleancoder.com/assets/ifElseSwitch.jpg\" alt=\"ifElseSwitch.jpg\" /></p>" +
                "<p><img src=\"//blog.cleancoder.com/assets/ifElseSwitchPolymorphism.jpg\" alt=\"ifElseSwitchPolymorphism.jpg\" /></p>";
        String expectedResult =
                "<p><img src=\"https://blog.cleancoder.com/assets/ifElseSwitch.jpg\" alt=\"ifElseSwitch.jpg\" /></p><p>" +
                "<img src=\"https://blog.cleancoder.com/assets/ifElseSwitchPolymorphism.jpg\" alt=\"ifElseSwitchPolymorphism.jpg\" /></p>";
        String result = ImageHandler.fixBrokenImageLinksInArticle(articleUrl, content);
        assertEquals(expectedResult, result);
    }
}
