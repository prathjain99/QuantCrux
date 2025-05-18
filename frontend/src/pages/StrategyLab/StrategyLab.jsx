import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/button";
import Header from "@/components/header";
import StrategyPreviewCard from "./components/StrategyPreviewCard";
import Input from "@/components/input";

const MOCK_STRATEGIES = [
  {
    id: "strat1",
    name: "Volatility Regime Momentum",
    createdAt: "2025-05-12T10:30:00Z",
    modulesUsed: ["Regime Detection", "Alpha Signal", "Backtesting"],
    performance: {
      sharpe: 1.34,
      cagr: 16.5,
      maxDrawdown: 7.2,
    },
  },
  {
    id: "strat2",
    name: "Balanced Growth Hedge",
    createdAt: "2025-05-15T09:45:00Z",
    modulesUsed: ["Risk Analysis", "Options Hedge"],
    performance: {
      sharpe: 1.1,
      cagr: 12.7,
      maxDrawdown: 6.5,
    },
  },
];

function StrategyLab() {
  const navigate = useNavigate();
  const [strategies, setStrategies] = useState([]);
  const [selectedStrategies, setSelectedStrategies] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    // Replace with API fetch
    setStrategies(MOCK_STRATEGIES);
  }, []);

  const handleNewStrategy = () => {
    navigate("/strategy-lab/create");
  };

  const toggleStrategySelection = (id) => {
    setSelectedStrategies((prev) =>
      prev.includes(id) ? prev.filter((sid) => sid !== id) : [...prev, id]
    );
  };

  const compareStrategies = () => {
    if (selectedStrategies.length < 2) {
      alert("Select at least two strategies to compare.");
      return;
    }
    navigate("/strategy-lab/compare", { state: { ids: selectedStrategies } });
  };

  const filteredStrategies = strategies.filter((s) =>
    s.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-12">
      <Header showHome />
      <h1 className="text-4xl font-bold mb-6 text-center">Strategy Lab</h1>

      {/* Create New Strategy */}
      <div className="text-center mb-10">
        <Button
          className="bg-gradient-to-r from-green-600 to-blue-600 px-6 py-3 text-lg rounded-xl hover:scale-105 transition"
          onClick={handleNewStrategy}
        >
          + Create New Strategy
        </Button>
      </div>

      {/* Search Box */}
      <div className="flex justify-center mb-6">
        <Input
          placeholder="Search your strategies..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="max-w-md"
        />
      </div>

      {/* List of Strategies */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
        {filteredStrategies.map((strategy) => (
          <StrategyPreviewCard
            key={strategy.id}
            strategy={strategy}
            isSelected={selectedStrategies.includes(strategy.id)}
            onToggle={() => toggleStrategySelection(strategy.id)}
            onClick={() => navigate(`/strategy-lab/strategy/${strategy.id}`)}
          />
        ))}
      </div>

      {/* Compare Button */}
      <div className="text-center mb-20">
        <Button
          onClick={compareStrategies}
          disabled={selectedStrategies.length < 2}
          className={`px-6 py-3 text-lg rounded-xl shadow-lg transition-transform hover:scale-105 ${
            selectedStrategies.length < 2
              ? "bg-gray-600 cursor-not-allowed"
              : "bg-gradient-to-r from-purple-600 to-pink-600"
          }`}
        >
          Compare Selected Strategies
        </Button>
      </div>
    </div>
  );
}

export default StrategyLab;
