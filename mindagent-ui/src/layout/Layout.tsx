import React from "react";

interface LayoutProps {
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  return (
    <div className="h-screen flex bg-[#f5f5f5] text-gray-900">
      {children}
    </div>
  );
};

export default Layout;
