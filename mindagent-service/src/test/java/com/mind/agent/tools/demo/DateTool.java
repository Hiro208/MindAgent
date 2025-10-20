package com.mind.agent.tools.demo;

import com.mind.agent.tools.Tool;
import com.mind.agent.tools.ToolType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTool implements Tool {
    @Override
    public String getName() {
        return "dateTool";
    }

    @Override
    public String getDescription() {
        return "Get the current date";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(name = "getDate", description = "Get the current date")
    public String getDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
