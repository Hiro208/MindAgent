import React, { useState, useRef, useEffect, useCallback } from "react";
import { Bubble } from "@ant-design/x";
import XMarkdown from "@ant-design/x-markdown";
import {
  ToolOutlined,
  CheckCircleOutlined,
  RobotOutlined,
  DownOutlined,
  RightOutlined,
} from "@ant-design/icons";
import type { ChatMessageVO, SseMessageType, ToolCall, ToolResponse } from "../../../types";

interface AgentChatHistoryProps {
  messages: ChatMessageVO[];
  displayAgentStatus?: boolean;
  agentStatusText?: string;
  agentStatusType?: SseMessageType;
}

const ToolCallDisplay: React.FC<{ toolCall: ToolCall }> = ({ toolCall }) => {
  let parsedArgs: Record<string, unknown> = {};
  try {
    parsedArgs = JSON.parse(toolCall.arguments) as Record<string, unknown>;
  } catch {

  }

  const argCount = Object.keys(parsedArgs).length;
  const argPreview = argCount > 0
    ? Object.keys(parsedArgs).slice(0, 2).join(", ") + (argCount > 2 ? "..." : "")
    : toolCall.arguments.slice(0, 50) + (toolCall.arguments.length > 50 ? "..." : "");

  return (
    <div className="text-xs text-gray-500 flex items-center gap-1.5">
      <ToolOutlined className="text-gray-500" />
      <span className="font-mono text-gray-700">{toolCall.name}</span>
      {argPreview && (
        <>
          <span className="text-gray-400">·</span>
          <span className="text-gray-500 truncate max-w-[200px]">{argPreview}</span>
        </>
      )}
    </div>
  );
};

const ToolResponseDisplay: React.FC<{ toolResponse: ToolResponse }> = ({
  toolResponse,
}) => {
  const [expanded, setExpanded] = useState(false);

  let parsedData: unknown = null;
  let isJson = false;
  let dataPreview = "";

  try {
    parsedData = JSON.parse(toolResponse.responseData);
    isJson = true;
    const jsonStr = JSON.stringify(parsedData);
    dataPreview = jsonStr.length > 100 ? jsonStr.slice(0, 100) + "..." : jsonStr;
  } catch {
    dataPreview = toolResponse.responseData.length > 100
      ? toolResponse.responseData.slice(0, 100) + "..."
      : toolResponse.responseData;
  }

  return (
    <div className="my-1.5 text-xs">
      <div
        className="flex items-center gap-2 text-gray-500 cursor-pointer hover:text-gray-700 transition-colors"
        onClick={() => setExpanded(!expanded)}
      >
        {expanded ? (
          <DownOutlined className="text-gray-400" />
        ) : (
          <RightOutlined className="text-gray-400" />
        )}
        <CheckCircleOutlined className="text-gray-600" />
        <span className="font-mono text-gray-700">{toolResponse.name}</span>
        <span className="text-gray-400">·</span>
        <span className="text-gray-500 truncate flex-1">{dataPreview}</span>
      </div>
      {expanded && (
        <div className="ml-5 mt-1.5 p-2 bg-gray-50 rounded border border-gray-200">
          <div className="text-xs text-gray-600 font-mono">
            {isJson ? (
              <pre className="whitespace-pre-wrap break-words overflow-x-auto max-h-60 overflow-y-auto">
                {JSON.stringify(parsedData, null, 2)}
              </pre>
            ) : (
              <div className="whitespace-pre-wrap break-words">
                {toolResponse.responseData}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

const AgentChatHistory: React.FC<AgentChatHistoryProps> = ({
  messages,
  displayAgentStatus = false,
  agentStatusText = "",
  agentStatusType,
}) => {

  const scrollContainerRef = useRef<HTMLDivElement>(null);

  const [isNearBottom, setIsNearBottom] = useState(true);

  const SCROLL_THRESHOLD = 20;

  const prevMessagesLengthRef = useRef(messages.length);

  const checkIfNearBottom = useCallback(() => {
    const container = scrollContainerRef.current;
    if (!container) return false;

    const { scrollTop, clientHeight, scrollHeight } = container;
    const distanceFromBottom = scrollHeight - scrollTop - clientHeight;
    return distanceFromBottom <= SCROLL_THRESHOLD;
  }, []);

  const scrollToBottom = useCallback(() => {
    const container = scrollContainerRef.current;
    if (!container) return;

    requestAnimationFrame(() => {
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    });
  }, []);

  const handleScroll = useCallback(() => {
    const nearBottom = checkIfNearBottom();
    setIsNearBottom(nearBottom);
  }, [checkIfNearBottom]);

  useEffect(() => {
    const container = scrollContainerRef.current;
    if (!container) return;

    const initTimer = setTimeout(() => {
      setIsNearBottom(checkIfNearBottom());
    }, 0);

    container.addEventListener("scroll", handleScroll, { passive: true });

    return () => {
      clearTimeout(initTimer);
      container.removeEventListener("scroll", handleScroll);
    };
  }, [handleScroll, checkIfNearBottom]);

  useEffect(() => {
    const hasNewMessage = messages.length > prevMessagesLengthRef.current;
    prevMessagesLengthRef.current = messages.length;

    if (hasNewMessage && isNearBottom) {
      scrollToBottom();
    }
  }, [messages, isNearBottom, scrollToBottom]);

  useEffect(() => {
    if (displayAgentStatus && isNearBottom) {
      scrollToBottom();
    }
  }, [displayAgentStatus, isNearBottom, scrollToBottom]);

  const getStatusLabel = () => {
    switch (agentStatusType) {
      case "AI_PLANNING":
        return "Planning";
      case "AI_THINKING":
        return "Thinking";
      case "AI_EXECUTING":
        return "Executing";
      default:
        return "Processing";
    }
  };

  return (
    <div
      ref={scrollContainerRef}
      className="flex-1 px-16 pt-6 overflow-y-scroll bg-[#f3f4f6]"
    >
      {messages.map((message) => {
        return (
          <div className="mb-4" key={message.id}>
            {message.role === "assistant" && (
              <Bubble
                content={
                  <div className="w-full">
                    {message.metadata?.toolCalls &&
                      message.metadata.toolCalls.length > 0 && (
                        <div className="mb-2 flex flex-wrap gap-2">
                          {message.metadata.toolCalls.map((toolCall) => (
                            <ToolCallDisplay key={toolCall.id} toolCall={toolCall} />
                          ))}
                        </div>
                      )}
                    {message.content && (
                      <div>
                        <XMarkdown
                          streaming={{ enableAnimation: false, hasNextChunk: true }}
                        >
                          {message.content}
                        </XMarkdown>
                      </div>
                    )}
                  </div>
                }
                placement="start"
              />
            )}
            {message.role === "tool" && message.metadata?.toolResponse && (
              <div className="flex justify-start">
                <div className="max-w-[85%]">
                  <ToolResponseDisplay toolResponse={message.metadata.toolResponse} />
                </div>
              </div>
            )}
            {message.role === "user" && (
              <Bubble content={message.content} placement="end" />
            )}
            {message.role === "system" && (
              <div className="flex justify-center">
                <div className="px-3 py-1 bg-white text-gray-600 text-xs rounded-full flex items-center gap-1 border border-gray-200">
                  <RobotOutlined />
                  <span>{message.content}</span>
                </div>
              </div>
            )}
          </div>
        );
      })}
      {displayAgentStatus && (
        <div className="mb-3">
          <div
            className="animate-pulse"
            style={{
              animation: "pulse 0.8s cubic-bezier(0.4, 0, 0.6, 1) infinite",
              filter: "brightness(1.02)",
            }}
          >
            <Bubble
              content={
                <span className="flex items-center gap-2">
                  <span
                    className="font-semibold text-gray-700"
                    style={{
                      animation:
                        "pulse 0.7s cubic-bezier(0.4, 0, 0.6, 1) infinite",
                      textShadow:
                        "0 0 4px rgba(0, 0, 0, 0.08)",
                      filter: "none",
                    }}
                  >
                    {getStatusLabel()}
                  </span>
                  <span className="text-gray-400">·</span>
                  <span className="text-gray-600">{agentStatusText}</span>
                </span>
              }
              placement="start"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default AgentChatHistory;
