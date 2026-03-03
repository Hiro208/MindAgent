import React, { useState, useMemo } from "react";
import { Card, Space, Typography, Select } from "antd";
import {
  BulbOutlined,
  MessageOutlined,
  RobotOutlined,
  DownOutlined,
} from "@ant-design/icons";
import { Sender } from "@ant-design/x";
import { useNavigate } from "react-router-dom";
import {
  type AgentVO,
  createChatMessage,
  createChatSession,
} from "../../../api/api.ts";
import { getAgentEmoji } from "../../../utils";
import { useChatSessions } from "../../../hooks/useChatSessions.ts";

const { Title, Text } = Typography;

interface DefaultAgentChatViewProps {
  handleSendMessage: (message: string) => void;
  loading: boolean;
  agents: AgentVO[];
}

const EmptyAgentChatView: React.FC<DefaultAgentChatViewProps> = ({
  loading,
  agents,
}) => {
  const [message, setMessage] = useState("");
  const [selectedAgentId, setSelectedAgentId] = useState<string | null>(null);

  const navigate = useNavigate();
  const { refreshChatSessions } = useChatSessions();

  // Generate emoji for each agent (will not be shown with color theme)
  const agentsWithEmoji = useMemo(() => {
    return agents.map((agent) => ({
      ...agent,
      emoji: getAgentEmoji(agent.id),
    }));
  }, [agents]);

  // 计算实际选中的 agent ID（如果用户没有选择，则使用默认的第一个）
  const effectiveAgentId = useMemo(() => {
    if (selectedAgentId) {
      return selectedAgentId;
    }
    return agents.length > 0 ? agents[0].id : null;
  }, [selectedAgentId, agents]);

  return (
    <div className="flex flex-col h-full bg-[#f3f4f6] text-gray-900">
      {/* Agent selector - top */}
      {agents.length > 0 && (
        <div className="border-b border-gray-200 bg-[#f9fafb] px-4 py-3">
          <div className="flex items-center justify-start">
            <Select
              value={effectiveAgentId}
              onChange={(value) => setSelectedAgentId(value)}
              style={{ width: 200 }}
              className="agent-selector"
              suffixIcon={<DownOutlined className="text-gray-400" />}
              placeholder="Select agent"
              optionRender={(option) => (
                <div className="flex items-center gap-2">
                  <span className="text-lg">
                    {agentsWithEmoji.find((a) => a.id === option.value)?.emoji}
                  </span>
                  <span className="text-sm">{option.label}</span>
                </div>
              )}
              options={agentsWithEmoji.map((agent) => ({
                value: agent.id,
                label: agent.name,
              }))}
            />
          </div>
        </div>
      )}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="max-w-2xl w-full space-y-6">
          <div className="text-center mb-8">
            <Title level={2} className="mb-2">
              Start a new conversation
            </Title>
            <Text type="secondary" className="text-base">
              Select an agent to start, or send a message to create a new chat.
            </Text>
          </div>
          <Space orientation="vertical" size="large" className="w-full">
            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-white border border-gray-200 flex items-center justify-center">
                  <RobotOutlined className="text-gray-800 text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    Intelligent chat
                  </Title>
                  <Text type="secondary">
                    Talk to the agent and get structured assistance.
                  </Text>
                </div>
              </Space>
            </Card>

            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-white border border-gray-200 flex items-center justify-center">
                  <BulbOutlined className="text-gray-800 text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    Knowledge Q&A
                  </Title>
                  <Text type="secondary">
                    Ask questions grounded in your knowledge base.
                  </Text>
                </div>
              </Space>
            </Card>

            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-white border border-gray-200 flex items-center justify-center">
                  <MessageOutlined className="text-gray-800 text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    Quick start
                  </Title>
                  <Text type="secondary">
                    Type a message below to start immediately.
                  </Text>
                </div>
              </Space>
            </Card>
          </Space>
        </div>
      </div>
      <div className="border-t border-gray-200 bg-white">
        {/* Input area */}
        <div className="px-4 pb-4 pt-4">
          <Sender
            onSubmit={async () => {
              if (!effectiveAgentId) return;
              console.log("发送消息", message);
              const response = await createChatSession({
                agentId: effectiveAgentId,
                title: message.slice(0, 20),
              });
              await createChatMessage({
                sessionId: response.chatSessionId ?? "",
                content: message,
                role: "user",
                agentId: effectiveAgentId,
              });
              // 刷新聊天会话列表
              await refreshChatSessions();
              setMessage("");
              navigate(
                `/chat/${response.chatSessionId}`,
              );
            }}
            value={message}
            loading={loading}
            placeholder="Type a message to start..."
            onChange={(value) => {
              setMessage(value);
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default EmptyAgentChatView;
