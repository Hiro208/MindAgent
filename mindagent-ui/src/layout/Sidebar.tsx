import React from "react";

interface SidebarProps {
  children: React.ReactNode;
}

const Sidebar: React.FC<SidebarProps> = ({ children }) => {
  return (
    <div
      className="h-full bg-[#f9fafb] border-r border-gray-200"
      style={{
        width: "320px",
      }}
    >
      {children}
    </div>
  );
};

export default Sidebar;
