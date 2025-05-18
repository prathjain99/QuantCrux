import React from "react";
import { Card } from "@/components/card";

export default function StrategyPreviewCard({
  strategy,
  isSelected,
  onToggle,
  onClick,
}) {
  const { name, createdAt, modulesUsed, performance } = strategy;

  return (
    <Card
      className={`bg-[#1C2433] p-4 rounded-xl shadow-lg cursor-pointer transition-transform hover:scale-[1.02] ${
        isSelected ? "border-4 border-blue-500" : ""
      }`}
      onClick={onToggle}
    >
      <div className="flex justify-between items-start">
        <h3 className="text-xl font-semibold">{name}</h3>
        <input
          type="checkbox"
          checked={isSelected}
          onChange={(e) => e.stopPropagation()}
          onClick={(e) => {
            e.stopPropagation();
            onToggle();
          }}
          className="w-5 h-5"
        />
      </div>

      <p className="text-sm text-gray-400 mb-2">
        Created on: {new Date(createdAt).toLocaleDateString()}
      </p>

      <p className="text-sm text-gray-300 mb-2">
        Modules Used:{" "}
        <span className="text-blue-400">{modulesUsed.join(", ")}</span>
      </p>

      <div className="text-sm text-gray-200 space-y-1 mb-3">
        {performance?.sharpe !== undefined && (
          <div>
            Sharpe Ratio: <span className="text-green-400">{performance.sharpe}</span>
          </div>
        )}
        {performance?.cagr !== undefined && (
          <div>
            CAGR: <span className="text-green-400">{performance.cagr}%</span>
          </div>
        )}
        {performance?.maxDrawdown !== undefined && (
          <div>
            Max Drawdown:{" "}
            <span className="text-red-400">{performance.maxDrawdown}%</span>
          </div>
        )}
      </div>

      <button
        className="bg-blue-600 text-white px-4 py-1 rounded-md hover:bg-blue-700"
        onClick={(e) => {
          e.stopPropagation();
          onClick();
        }}
      >
        View Details
      </button>
    </Card>
  );
}
