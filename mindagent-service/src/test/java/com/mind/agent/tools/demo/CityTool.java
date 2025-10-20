package com.mind.agent.tools.demo;

import com.mind.agent.tools.Tool;
import com.mind.agent.tools.ToolType;
import org.springframework.stereotype.Component;

@Component
public class CityTool implements Tool {
    @Override
    public String getName() {
        return "cityTool";
    }

    @Override
    public String getDescription() {
        return "Get the current city";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(name = "getCity", description = "Get the current city")
    public String getCity() {
        return "Shenzhen";
    }
}
