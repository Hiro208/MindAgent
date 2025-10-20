package com.mind;

import com.mind.service.impl.MarkdownParserServiceImpl;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class MarkdownParserServiceTest {
    private final MarkdownParserServiceImpl parser = new MarkdownParserServiceImpl();

    @Test
    void preservesPreambleTablesAndCode() {
        String body = "| A | B |\n|---|---|\n| 1 | 2 |\n\n```java\nint x = 1;\n```";
        var sections = parser.parseMarkdown(stream("intro\n\n# Title\n\n" + body));
        assertEquals(2, sections.size());
        assertEquals("intro", sections.get(0).getContent());
        assertEquals("Title", sections.get(1).getTitle());
        assertEquals(body, sections.get(1).getContent());
    }

    @Test
    void concurrentDocumentsRemainIndependent() {
        IntStream.range(0, 100).parallel().forEach(i -> {
            var sections = parser.parseMarkdown(stream("# Doc " + i + "\n\ncontent " + i));
            assertEquals("Doc " + i, sections.get(0).getTitle());
            assertEquals("content " + i, sections.get(0).getContent());
        });
    }

    private ByteArrayInputStream stream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
