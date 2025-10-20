package com.mind.agent.tools.demo;

import com.mind.agent.tools.Tool;
import com.mind.agent.tools.ToolType;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool implements Tool {

    @Override
    public String getName() {
        return "weatherTool";
    }

    @Override
    public String getDescription() {
        return "Get weather";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(name = "weather", description = "Get weather")
    public String getWeather(String city, String date) {

        return city + date + " weather: sunny becoming cloudy, temperature 25°C，humidity 60%";
    }
}
