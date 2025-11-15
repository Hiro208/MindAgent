export const getAgentEmoji = (agentId: string): string => {

  const EMOJI_LIST = [
    "🤖",
    "🎯",
    "🚀",
    "💡",
    "🔮",
    "⚡",
    "🌟",
    "🎨",
    "🔧",
    "📚",
  ];
  let hash = 0;
  for (let i = 0; i < agentId.length; i++) {
    hash = (hash << 5) - hash + agentId.charCodeAt(i);
    hash = hash & hash; // Convert to 32bit integer
  }
  const index = Math.abs(hash) % EMOJI_LIST.length;
  return EMOJI_LIST[index];
};

export const getKnowledgeBaseEmoji = (knowledgeBaseId: string): string => {

  const KNOWLEDGE_BASE_EMOJI_LIST = [
    "📚",
    "📖",
    "📝",
    "📋",
    "📑",
    "📄",
    "📃",
    "📊",
    "📈",
    "📉",
  ];

  let hash = 0;
  for (let i = 0; i < knowledgeBaseId.length; i++) {
    hash = (hash << 5) - hash + knowledgeBaseId.charCodeAt(i);
    hash = hash & hash; // Convert to 32bit integer
  }
  const index = Math.abs(hash) % KNOWLEDGE_BASE_EMOJI_LIST.length;
  return KNOWLEDGE_BASE_EMOJI_LIST[index];
};

export const formatDateTime = (dateString?: string): string => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60));
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60));
      return minutes <= 0 ? "Just now" : `${minutes} minutes ago`;
    }
    return `${hours} hours ago`;
  } else if (days === 1) {
    return "Yesterday";
  } else if (days < 7) {
    return `${days} days ago`;
  } else {
    return date.toLocaleDateString("zh-CN", {
      month: "short",
      day: "numeric",
    });
  }
};
