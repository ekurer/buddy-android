package app.buddy.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Splits text into plain parts and markdown links, [text](https://url). No Android classes, so it runs in plain JVM tests. */
final class MarkdownLinks {
    private static final Pattern LINK = Pattern.compile("\\[([^\\[\\]\\r\\n]+)\\]\\((https?://[^\\s()]+)\\)");

    /** One piece of the text: plain when url is null, otherwise a link showing {@code text}. */
    static final class Part {
        final String text;
        final String url;

        Part(String text, String url) {
            this.text = text;
            this.url = url;
        }
    }

    private MarkdownLinks() {}

    static List<Part> split(CharSequence src) {
        List<Part> parts = new ArrayList<>();
        Matcher m = LINK.matcher(src);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) parts.add(new Part(src.subSequence(last, m.start()).toString(), null));
            parts.add(new Part(m.group(1), m.group(2)));
            last = m.end();
        }
        if (last < src.length()) parts.add(new Part(src.subSequence(last, src.length()).toString(), null));
        return parts;
    }
}
