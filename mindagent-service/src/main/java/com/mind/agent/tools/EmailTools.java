package com.mind.agent.tools;

import com.mind.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailTools implements Tool {

    private final EmailService emailService;

    public EmailTools(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public String getName() {
        return "emailTool";
    }

    @Override
    public String getDescription() {
        return "Sends email through QQ Mail asynchronously without blocking tool execution.";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "sendEmail",
            description = "Send email asynchronously. Required parameters: to (recipient email address), subject, and content (message body). The tool returns immediately while delivery runs in the background."
    )
    public String sendEmail(String to, String subject, String content) {

        if (to == null || to.trim().isEmpty()) {
            return "Error: recipient email address is required";
        }
        if (subject == null || subject.trim().isEmpty()) {
            return "Error: email subject is required";
        }
        if (content == null || content.trim().isEmpty()) {
            return "Error: email body is required";
        }

        if (!to.contains("@")) {
            return "Error: invalid recipient email address";
        }

        emailService.sendEmailAsync(to.trim(), subject.trim(), content.trim());

        log.info("Email queued for asynchronous delivery; recipient: {}, Subject: {}", to, subject);
        return String.format("Email queued for delivery!\nRecipient: %s\nSubject: %s\nEmail delivery is running in the background...", to, subject);
    }
}
