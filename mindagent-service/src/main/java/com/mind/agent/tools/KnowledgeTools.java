package com.mind.agent.tools;

import com.mind.service.RagService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeTools implements Tool {

    private final RagService ragService;

    public KnowledgeTools(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public String getName() {
        return "KnowledgeTool";
    }

    @Override
    public String getDescription() {
        return "Search a knowledge base using RAG. Provide its ID and query to retrieve relevant passages.";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "KnowledgeTool",
            description = "Search a knowledge base using RAG. Provide kbsId and query to retrieve the most relevant passages."
    )
    public String knowledgeQuery(String kbsId, String query) {
        List<String> strings = ragService.similaritySearch(kbsId, query);
        return String.join("\n", strings);
    }
}
