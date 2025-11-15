import React from "react";

interface ContentProps {
  children: React.ReactNode;
}

const Content: React.FC<ContentProps> = ({ children }) => {
  return (
    <div className="h-full flex-1 bg-[#f3f4f6] border-l border-gray-200">
      {children}
    </div>
  );
};

export default Content;
