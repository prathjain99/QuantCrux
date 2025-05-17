import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "@/components/header";
import { Card, CardContent } from "@/components/card";
import { Button } from "@/components/button";

// Use the same MOCK_SESSIONS from Dashboard or import it if externalized
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
  // ...more mock sessions
];

export default function SessionDetails() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const [session, setSession] = useState(null);

  useEffect(() => {
    // In real app, fetch session by sessionId from API/backend
    const foundSession = MOCK_SESSIONS.find((s) => s.id === sessionId);
    setSession(foundSession);
  }, [sessionId]);

  if (!session) {
    return (
      <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14 font-sans">
        <Header showHome />
        <div className="text-center mt-20 text-gray-400 text-xl">Session not found.</div>
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

  return (
    <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14 font-sans">
      <Header showHome />
      <Button
        onClick={() => navigate("/dashboard")}
        className="mb-6 bg-gray-700 hover:bg-gray-600 rounded-md px-4 py-2"
      >
        ← Back to Dashboard
      </Button>

      <h1 className="text-4xl font-bold mb-4">{session.name}</h1>
      <p className="text-gray-400 mb-8">
        Module: <span className="text-blue-400">{session.module}</span> |{" "}
        Date: <span>{new Date(session.timestamp).toLocaleString()}</span>
      </p>

      <Card className="bg-[#1C2433] rounded-xl shadow-lg max-w-3xl mx-auto p-6">
        <h2 className="text-2xl font-semibold mb-4">Session Metrics</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-gray-300">
          {Object.entries(session.metricsSummary).map(([key, val]) => (
            <div key={key} className="capitalize border-b border-gray-600 pb-2">
              <span className="font-medium">{key.replace(/([A-Z])/g, " $1")}</span>:{" "}
              <span className="text-blue-400">{val}</span>
            </div>
          ))}
        </div>
      </Card>

      {/* Future expansion: charts, detailed logs, notes, attachments */}
      <Card className="bg-[#1C2433] rounded-xl shadow-lg max-w-3xl mx-auto mt-10 p-6">
        <h2 className="text-2xl font-semibold mb-4">Additional Details</h2>
        <p className="text-gray-400">More insights, charts, or notes will be displayed here.</p>
      </Card>
    </div>
  );
}
