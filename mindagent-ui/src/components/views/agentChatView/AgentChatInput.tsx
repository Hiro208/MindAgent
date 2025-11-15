import React, { useState } from "react";
import { Sender } from "@ant-design/x";

interface AgentChatInputProps {
  onSend: (message: string) => void;
}

const AgentChatInput: React.FC<AgentChatInputProps> = ({ onSend }) => {
  const [message, setMessage] = useState("");

  return (
    <Sender
      onSubmit={() => {
        onSend(message.trim());
        setMessage("");
      }}
      placeholder="Type a message..."
      value={message}
      onChange={setMessage}
    />
  );
};

export default AgentChatInput;
