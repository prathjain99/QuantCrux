import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent } from "@/components/card";
import { Button } from "@/components/button";
import Header from "@/components/header";
import Input from "@/components/input";

const MOCK_SESSIONS = [
  {
    id: "s1",
    userId: "u1",
    module: "Portfolio Optimization",
    name: "Tech Growth Portfolio",
    timestamp: "2025-05-17T10:12:00Z",
    metricsSummary: {
      expectedReturn: 14.2,
      sharpeRatio: 1.3,
      volatility: 9.1,
    },
  },
  {
    id: "s2",
    userId: "u1",
    module: "Backtesting",
    name: "Momentum Strategy Test",
    timestamp: "2025-05-16T14:22:00Z",
    metricsSummary: {
      totalReturn: 22.5,
      maxDrawdown: 8.7,
      sharpeRatio: 1.5,
    },
  },
  {
    id: "s3",
    userId: "u1",
    module: "Alpha Signal",
    name: "Earnings Surprise Signal",
    timestamp: "2025-05-15T18:45:00Z",
    metricsSummary: {
      alpha: 0.05,
      signalStrength: 0.85,
    },
  },
  // Add more mock sessions here
];

const MOCK_ANALYTICS = {
  totalSessions: 35,
  mostUsedModule: "Portfolio Optimization",
  averageSharpeRatio: 1.25,
  totalBacktestsRun: 12,
  totalOptimizationsRun: 15,
};

export default function Dashboard() {
  const navigate = useNavigate();

  const [sessions, setSessions] = useState([]);
  const [filteredSessions, setFilteredSessions] = useState([]);
  const [selectedSessions, setSelectedSessions] = useState([]);
  const [filterModule, setFilterModule] = useState("");
  const [searchTerm, setSearchTerm] = useState("");

  // Fetch sessions & analytics on mount (replace with actual API call)
  useEffect(() => {
    setSessions(MOCK_SESSIONS);
    setFilteredSessions(MOCK_SESSIONS);
  }, []);

  // Filter & search sessions
  useEffect(() => {
    let filtered = sessions;

    if (filterModule) {
      filtered = filtered.filter(
        (s) => s.module.toLowerCase() === filterModule.toLowerCase()
      );
    }

    if (searchTerm) {
      filtered = filtered.filter(
        (s) =>
          s.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          s.module.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    setFilteredSessions(filtered);
    setSelectedSessions([]); // Clear selection on filter/search change
  }, [filterModule, searchTerm, sessions]);

  // Handle session selection for comparison
  const toggleSelectSession = (sessionId) => {
    setSelectedSessions((prev) =>
      prev.includes(sessionId)
        ? prev.filter((id) => id !== sessionId)
        : [...prev, sessionId]
    );
  };

  // Navigate to session details
  const viewSessionDetails = (sessionId) => {
    // navigate(`/session-details/${sessionId}`);
    navigate("session-details");
  };

  // Navigate to comparison view with selected sessions
  const compareSessions = () => {
    if (selectedSessions.length < 2) {
      alert("Select at least two sessions to compare.");
      return;
    }
    navigate("/dashboard/model-comparison", { state: { sessionIds: selectedSessions } });
  };

  return (
    <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14 font-sans">
      <Header showHome />
      <h1 className="text-4xl font-bold mb-8 text-center">Dashboard</h1>

      {/* Filter & Search */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-8 gap-4">
        <select
          value={filterModule}
          onChange={(e) => setFilterModule(e.target.value)}
          className="bg-[#1C2433] text-white px-4 py-2 rounded-md"
        >
          <option value="">All Modules</option>
          <option value="Regime">Regime</option>
          <option value="Alpha Signal">Alpha Signal</option>
          <option value="Backtesting">Backtesting</option>
          <option value="Portfolio Optimization">Portfolio Optimization</option>
          <option value="Risk Analysis">Risk Analysis</option>
          <option value="Options Pricing">Options Pricing</option>
        </select>

        <Input
          placeholder="Search sessions or modules..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="max-w-md"
        />
      </div>

      {/* Sessions List */}
      <div className="mb-8">
        <h2 className="text-2xl font-semibold mb-4">Your Saved Sessions</h2>
        {filteredSessions.length === 0 ? (
          <p className="text-gray-400">No sessions found.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {filteredSessions.map((session) => (
              <Card
                key={session.id}
                className={`bg-[#1C2433] p-4 rounded-xl shadow-lg cursor-pointer transition-transform hover:scale-[1.02] ${
                  selectedSessions.includes(session.id)
                    ? "border-4 border-blue-500"
                    : ""
                }`}
                onClick={() => toggleSelectSession(session.id)}
              >
                <h3 className="text-xl font-semibold mb-1">{session.name}</h3>
                <p className="text-sm text-gray-300 mb-2">{session.module}</p>
                <div className="text-xs text-gray-400 mb-2">
                  {new Date(session.timestamp).toLocaleString()}
                </div>
                <div className="text-sm space-y-1">
                  {Object.entries(session.metricsSummary).map(([key, val]) => (
                    <div key={key} className="capitalize">
                      {key.replace(/([A-Z])/g, " $1")}:{" "}
                      <span className="text-blue-400">{val}</span>
                    </div>
                  ))}
                </div>
                <button
                  className="mt-3 bg-blue-600 text-white px-3 py-1 rounded-md hover:bg-blue-700"
                  onClick={(e) => {
                    e.stopPropagation();
                    viewSessionDetails(session.id);
                  }}
                >
                  View Details
                </button>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Compare Button */}
      <div className="text-center mb-12">
        <Button
          onClick={compareSessions}
          disabled={selectedSessions.length < 2}
          className={`px-6 py-3 text-lg rounded-xl shadow-lg transition-transform hover:scale-105 ${
            selectedSessions.length < 2
              ? "bg-gray-600 cursor-not-allowed"
              : "bg-gradient-to-r from-blue-600 to-purple-600"
          }`}
        >
          Compare Selected Sessions
        </Button>
      </div>

      {/* User Analytics Summary */}
      <div className="mb-12">
        <h2 className="text-2xl font-semibold mb-4">User Analytics</h2>
        <Card className="bg-[#1C2433] p-6 rounded-xl shadow-lg max-w-xl mx-auto">
          <ul className="space-y-2 text-gray-300 text-lg">
            <li>Total Sessions Created: {MOCK_ANALYTICS.totalSessions}</li>
            <li>Most Used Module: {MOCK_ANALYTICS.mostUsedModule}</li>
            <li>Average Sharpe Ratio: {MOCK_ANALYTICS.averageSharpeRatio}</li>
            <li>Total Backtests Run: {MOCK_ANALYTICS.totalBacktestsRun}</li>
            <li>Total Optimizations Run: {MOCK_ANALYTICS.totalOptimizationsRun}</li>
          </ul>
        </Card>
      </div>

      {/* Profile Section Placeholder */}
      <div className="mb-12 max-w-xl mx-auto">
        <h2 className="text-2xl font-semibold mb-4">User Profile</h2>
        <Card className="bg-[#1C2433] p-6 rounded-xl shadow-lg">
          <p>Profile management coming soon...</p>
        </Card>
      </div>
    </div>
  );
}
