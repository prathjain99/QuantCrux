import React, { useState } from "react";

export default function Tabs({ tabs }) {
  const [activeTab, setActiveTab] = useState(tabs[0].label);

  return (
    <div className="w-full">
      {/* Tab Headers */}
      <div className="flex border-b border-gray-700 mb-4">
        {tabs.map((tab) => (
          <button
            key={tab.label}
            onClick={() => setActiveTab(tab.label)}
            className={`px-4 py-2 text-sm font-medium transition duration-200 ${
              activeTab === tab.label
                ? "text-blue-400 border-b-2 border-blue-500"
                : "text-gray-400 hover:text-blue-300"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Active Tab Content */}
      <div className="bg-[#1C2433] rounded-lg p-4 shadow-md border border-gray-700">
        {tabs.find((tab) => tab.label === activeTab)?.content}
      </div>
    </div>
  );
}
