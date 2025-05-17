import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Header from "@/components/header";
import { Card } from "@/components/card";
import { Button } from "@/components/button";

// Reuse or import your mock sessions data
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
  // Add more mock sessions as needed
];

export default function SessionCompare() {
  const navigate = useNavigate();
  const location = useLocation();
  const { sessionIds } = location.state || { sessionIds: [] };

  const [sessions, setSessions] = useState([]);

  useEffect(() => {
    // Fetch sessions by IDs (mocking here)
    const matchedSessions = MOCK_SESSIONS.filter((s) => sessionIds.includes(s.id));
    setSessions(matchedSessions);
  }, [sessionIds]);

  if (!sessionIds || sessionIds.length < 2) {
    return (
      <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14 font-sans">
        <Header showHome />
        <div className="text-center mt-20 text-gray-400 text-xl">
          Please select at least two sessions to compare.
        </div>
        <div className="text-center mt-8">
          <Button
            onClick={() => navigate("/dashboard")}
            className="bg-gradient-to-r from-blue-600 to-purple-600 px-6 py-3 rounded-xl"
          >
            Back to Dashboard
          </Button>
        </div>
      </div>
    );
  }

  // Collect all unique metric keys from all sessions
  const allMetricKeys = Array.from(
    new Set(sessions.flatMap((s) => Object.keys(s.metricsSummary)))
  );

  // Helper to format timestamp nicely
  const formatDate = (ts) => new Date(ts).toLocaleString();

  return (
    <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14 font-sans">
      <Header showHome />
      <Button
        onClick={() => navigate("/dashboard")}
        className="mb-6 bg-gray-700 hover:bg-gray-600 rounded-md px-4 py-2"
      >
        ← Back to Dashboard
      </Button>

      <h1 className="text-4xl font-bold mb-8 text-center">Compare Sessions</h1>

      <div className="overflow-auto">
        <table className="min-w-full table-auto border-collapse border border-gray-700 rounded-lg">
          <thead>
            <tr className="bg-[#1C2433]">
              <th className="border border-gray-600 p-4 text-left sticky top-0 z-10">Metric / Session</th>
              {sessions.map((session) => (
                <th
                  key={session.id}
                  className="border border-gray-600 p-4 text-center sticky top-0 z-10"
                  style={{ minWidth: "220px" }}
                >
                  <div className="font-semibold mb-1">{session.name}</div>
                  <div className="text-sm text-gray-400">{session.module}</div>
                  <div className="text-xs text-gray-500 mt-1">{formatDate(session.timestamp)}</div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {allMetricKeys.map((metric) => (
              <tr
                key={metric}
                className="border-t border-gray-700 hover:bg-[#2A3350] transition-colors"
              >
                <td className="border border-gray-600 p-3 font-medium capitalize">
                  {metric.replace(/([A-Z])/g, " $1")}
                </td>
                {sessions.map((session) => {
                  const value = session.metricsSummary[metric];
                  return (
                    <td
                      key={session.id + metric}
                      className={`border border-gray-600 p-3 text-center ${
                        value === undefined ? "text-gray-600 italic" : "text-blue-400 font-semibold"
                      }`}
                    >
                      {value !== undefined ? value : "-"}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Optional: Add summary or highlight differences feature here */}

    </div>
  );
}
