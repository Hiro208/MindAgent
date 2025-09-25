package com.mind.agent.tools;

import org.springframework.stereotype.Component;

// @Component
public class DirectAnswerTool implements Tool {

    @Override
    public String getName() {
        return "directAnswer";
    }

    @Override
    public String getDescription() {
        return "Return a natural language answer when no operation is required.";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "directAnswer",
            description = "Answer the user directly when no task plan or other tool is needed."
    )
    public void directAnswer() {}
}
