package app.buddy.assistant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class MarkdownLinksTest {
    private static void assertPlain(MarkdownLinks.Part p, String text) {
        assertEquals(text, p.text);
        assertNull(p.url);
    }

    private static void assertLink(MarkdownLinks.Part p, String text, String url) {
        assertEquals(text, p.text);
        assertEquals(url, p.url);
    }

    @Test
    public void emptyText_hasNoParts() {
        assertTrue(MarkdownLinks.split("").isEmpty());
    }

    @Test
    public void plainText_isOnePlainPart() {
        List<MarkdownLinks.Part> p = MarkdownLinks.split("Nothing to link here.");
        assertEquals(1, p.size());
        assertPlain(p.get(0), "Nothing to link here.");
    }

    @Test
    public void markdownLink_aloneBecomesOneLink() {
        List<MarkdownLinks.Part> p = MarkdownLinks.split("[Buddy](https://github.com/ghorbelhamdi/buddy-android)");
        assertEquals(1, p.size());
        assertLink(p.get(0), "Buddy", "https://github.com/ghorbelhamdi/buddy-android");
    }

    @Test
    public void markdownLink_keepsSurroundingText() {
        List<MarkdownLinks.Part> p = MarkdownLinks.split("See [the docs](http://example.com/a?b=1#c) now.");
        assertEquals(3, p.size());
        assertPlain(p.get(0), "See ");
        assertLink(p.get(1), "the docs", "http://example.com/a?b=1#c");
        assertPlain(p.get(2), " now.");
    }

    @Test
    public void multipleLinks_areAllFound() {
        List<MarkdownLinks.Part> p = MarkdownLinks.split("[a](https://a.example) and [b](https://b.example)");
        assertEquals(3, p.size());
        assertLink(p.get(0), "a", "https://a.example");
        assertPlain(p.get(1), " and ");
        assertLink(p.get(2), "b", "https://b.example");
    }

    @Test
    public void hebrewText_aroundLink() {
        List<MarkdownLinks.Part> p = MarkdownLinks.split("ראה [האתר שלנו](https://example.com/he) עכשיו");
        assertEquals(3, p.size());
        assertPlain(p.get(0), "ראה ");
        assertLink(p.get(1), "האתר שלנו", "https://example.com/he");
        assertPlain(p.get(2), " עכשיו");
    }

    @Test
    public void bareUrlAndEmail_stayPlainForLinkify() {
        String s = "Go to https://example.com or mail me@example.com";
        List<MarkdownLinks.Part> p = MarkdownLinks.split(s);
        assertEquals(1, p.size());
        assertPlain(p.get(0), s);
    }

    @Test
    public void bareUrlNextToMarkdownLink_staysPlain() {
        List<MarkdownLinks.Part> p = MarkdownLinks.split("https://x.example [y](https://y.example)");
        assertEquals(2, p.size());
        assertPlain(p.get(0), "https://x.example ");
        assertLink(p.get(1), "y", "https://y.example");
    }

    @Test
    public void parenthesesAfterLink_arePlainText() {
        List<MarkdownLinks.Part> p = MarkdownLinks.split("[a](https://a.example) (see above)");
        assertEquals(2, p.size());
        assertLink(p.get(0), "a", "https://a.example");
        assertPlain(p.get(1), " (see above)");
    }

    @Test
    public void parenthesesInsideUrl_isNotALink() {
        String s = "[a](https://en.wikipedia.org/wiki/Foo_(bar))";
        List<MarkdownLinks.Part> p = MarkdownLinks.split(s);
        assertEquals(1, p.size());
        assertPlain(p.get(0), s);
    }

    @Test
    public void malformedMarkdown_isLeftAlone() {
        String[] cases = {
            "[text](https://missing.close",
            "[text] (https://space.example)",
            "[text](ftp://other.example)",
            "[text](example.com)",
            "[](https://empty.example)",
            "[a [b]](https://nested.example)",
            "[two\nlines](https://newline.example)",
            "[text](https://has space.example)",
        };
        for (String s : cases) {
            List<MarkdownLinks.Part> p = MarkdownLinks.split(s);
            for (MarkdownLinks.Part part : p) assertNull(s, part.url);
            StringBuilder joined = new StringBuilder();
            for (MarkdownLinks.Part part : p) joined.append(part.text);
            assertEquals(s, joined.toString());
        }
    }
}
