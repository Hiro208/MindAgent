package com.mind.service.impl;

import com.mind.service.MarkdownParserService;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Splits Markdown at top-level headings while retaining source formatting. */
@Service
public class MarkdownParserServiceImpl implements MarkdownParserService {
    @Override
    public List<MarkdownSection> parseMarkdown(InputStream inputStream) {
        final String source;
        try {
            source = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read Markdown", e);
        }
        // Parser and source belong to this request, so concurrent uploads share no state.
        Node document = Parser.builder().build().parse(source);
        List<MarkdownSection> sections = new ArrayList<>();
        String title = "Body";
        int contentStart = 0;
        for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Heading heading) {
                addSection(sections, title, source.substring(contentStart, heading.getStartOffset()));
                title = heading.getText().toString().trim();
                contentStart = heading.getEndOffset();
            }
        }
        addSection(sections, title, source.substring(contentStart));
        return sections;
    }

    private void addSection(List<MarkdownSection> sections, String title, String content) {
        String trimmed = content.trim();
        if (!trimmed.isEmpty()) {
            sections.add(new MarkdownSection(title.isEmpty() ? "Body" : title, trimmed));
        }
    }
}
