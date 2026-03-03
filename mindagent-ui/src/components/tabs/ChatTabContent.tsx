import React, { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Button, Divider, Popconfirm } from "antd";
import {
  PlusOutlined,
  MessageOutlined,
  DeleteOutlined,
} from "@ant-design/icons";
import { useChatSessions } from "../../hooks/useChatSessions.ts";
import { useAgents } from "../../hooks/useAgents.ts";

const ChatTabContent: React.FC = () => {
  const navigate = useNavigate();
  const { chatSessions, loading, deleteChatSession } = useChatSessions();
  const { agents } = useAgents();

  // 创建 agentId 到 agent 的映射
  const agentMap = useMemo(() => {
    const map = new Map<string, string>();
    agents.forEach((agent) => {
      map.set(agent.id, agent.name);
    });
    return map;
  }, [agents]);

  const handleCreateNewChat = () => {
    navigate("/chat");
  };

  const handleSelectChatSession = (chatSessionId: string) => {
    navigate(`/chat/${chatSessionId}`);
  };

  const handleDeleteChatSession = async (chatSessionId: string) => {
    await deleteChatSession(chatSessionId);
  };

  // Format display title
  const getDisplayTitle = (session: { title?: string; agentId: string }) => {
    if (session.title) {
      return session.title;
    }
    const agentName = agentMap.get(session.agentId);
    return agentName ? `Chat with ${agentName}` : "New chat";
  };

  return (
    <div className="flex flex-col h-full text-gray-900">
      <Button
        color="geekblue"
        variant="filled"
        icon={<PlusOutlined />}
        onClick={handleCreateNewChat}
        className="w-full"
      >
        New chat
      </Button>
      <Divider className="border-gray-200" />
      <div className="flex-1 min-h-0 overflow-y-auto bg-white rounded-lg border border-gray-200">
        {loading ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-400">
            <p className="text-sm">Loading...</p>
          </div>
        ) : chatSessions.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-400">
            <MessageOutlined className="text-4xl mb-2" />
            <p className="text-sm">No chat history</p>
            <p className="text-xs mt-1">Click the button above to start one.</p>
          </div>
        ) : (
          <div className="space-y-1.5 p-1.5">
            {chatSessions.map((session) => (
              <div
                key={session.id}
                onClick={() => handleSelectChatSession(session.id)}
                className="w-full px-3 py-2.5 rounded-lg bg-white cursor-pointer transition-all hover:bg-gray-50 hover:shadow-sm group relative border border-gray-200"
              >
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-lg bg-white border border-gray-200 flex items-center justify-center shrink-0 text-lg mt-0.5">
                    <MessageOutlined />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="font-medium text-gray-900 truncate">
                      {getDisplayTitle(session)}
                    </div>
                  </div>
                  <div onClick={(e) => e.stopPropagation()}>
                    <Popconfirm
                      title="Delete this chat?"
                      description="This action cannot be undone."
                      onConfirm={() => handleDeleteChatSession(session.id)}
                      okText="Delete"
                      cancelText="Cancel"
                    >
                      <Button
                        type="text"
                        size="small"
                        icon={<DeleteOutlined />}
                        className="opacity-0 group-hover:opacity-100 transition-opacity shrink-0"
                        onClick={(e) => e.stopPropagation()}
                        danger
                      />
                    </Popconfirm>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatTabContent;
