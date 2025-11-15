import React, { useMemo } from "react";
import { Button, Divider } from "antd";
import { PlusOutlined, BookOutlined } from "@ant-design/icons";
import type { KnowledgeBase } from "../../types";
import { getKnowledgeBaseEmoji } from "../../utils";

interface KnowledgeBaseTabContentProps {
  knowledgeBases: KnowledgeBase[];
  onCreateKnowledgeBaseClick?: () => void;
  onSelectKnowledgeBase?: (knowledgeBaseId: string) => void;
}

const KnowledgeBaseTabContent: React.FC<KnowledgeBaseTabContentProps> = ({
  knowledgeBases,
  onCreateKnowledgeBaseClick,
  onSelectKnowledgeBase,
}) => {
  // Precompute knowledge base meta (emoji kept for potential use)
  const knowledgeBasesWithEmoji = useMemo(() => {
    return knowledgeBases.map((kb) => ({
      ...kb,
      emoji: getKnowledgeBaseEmoji(kb.knowledgeBaseId),
    }));
  }, [knowledgeBases]);

  return (
    <div className="flex flex-col h-full text-gray-900">
      <Button
        color="geekblue"
        variant="filled"
        icon={<PlusOutlined />}
        onClick={onCreateKnowledgeBaseClick}
        className="w-full"
      >
        New knowledge base
      </Button>
      <Divider className="border-gray-200" />
      <div className="flex-1 overflow-y-scroll rounded-lg bg-white border border-gray-200">
        {knowledgeBases.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-400">
            <BookOutlined className="text-4xl mb-2" />
            <p className="text-sm">No knowledge bases</p>
            <p className="text-xs mt-1">Click the button above to create one.</p>
          </div>
        ) : (
          <div className="space-y-1.5 p-1.5">
            {knowledgeBasesWithEmoji.map((kb) => (
              <div
                key={kb.knowledgeBaseId}
                onClick={() => onSelectKnowledgeBase?.(kb.knowledgeBaseId)}
                className="w-full px-3 py-2.5 rounded-lg bg-white cursor-pointer transition-all hover:bg-gray-50 hover:shadow-sm border border-gray-200"
              >
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-lg bg-white border border-gray-200 flex items-center justify-center shrink-0 text-sm font-medium mt-0.5">
                    {kb.name.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="font-medium text-gray-900 truncate">
                      {kb.name}
                    </div>
                    {kb.description && (
                      <div className="text-xs text-gray-500 mt-1 line-clamp-2">
                        {kb.description}
                      </div>
                    )}
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

export default KnowledgeBaseTabContent;
