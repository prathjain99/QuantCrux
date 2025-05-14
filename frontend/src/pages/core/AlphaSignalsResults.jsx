import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent } from "@/components/card";
import { Button } from "@/components/button";
import { motion } from "framer-motion";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import Table from "@/components/table"; // Assuming you're using a Table component for displaying data

export default function AlphaSignalResults() {
  const [signalResults, setSignalResults] = useState([]);
  const [metrics, setMetrics] = useState({
    alpha: 0,
    sharpeRatio: 0,
    maxDrawdown: 0,
    accuracy: 0,
  });

    const navigate = useNavigate();

  // Example dummy data for the chart and table (to be replaced by real data from backend)
  const exampleResults = {
    signals: [
      { id: 1, name: "Signal A", alpha: 0.05, sharpeRatio: 1.2, maxDrawdown: 0.15, performance: [20, 40, 60, 80, 100] },
      { id: 2, name: "Signal B", alpha: 0.03, sharpeRatio: 0.9, maxDrawdown: 0.18, performance: [10, 30, 50, 70, 90] },
    ],
    metrics: {
      alpha: 0.04,
      sharpeRatio: 1.05,
      maxDrawdown: 0.16,
      accuracy: 0.75,
    },
  };

  useEffect(() => {
    // In a real scenario, fetch the data from an API or backend
    setSignalResults(exampleResults.signals);
    setMetrics(exampleResults.metrics);
  }, []);

  return (
    <div className="min-h-screen bg-[#0B1120] px-6 md:px-24 py-14 text-[#E2E8F0] font-sans">
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="text-center mb-10"
      >
        <h1 className="text-4xl font-bold text-white mb-4">Alpha Signal Discovery Results</h1>
        <p className="text-gray-400 max-w-2xl mx-auto">
          Explore the discovered alpha signals, their performance metrics, and comparisons with benchmarks.
        </p>
      </motion.div>

      {/* Results Overview Section */}
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-10"
      >
        {/* Left Card (Overview) */}
        <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
          <CardContent className="space-y-5 py-6">
            <h3 className="text-xl font-semibold">Performance Metrics</h3>
            <p><strong>Alpha:</strong> {metrics.alpha}</p>
            <p><strong>Sharpe Ratio:</strong> {metrics.sharpeRatio}</p>
            <p><strong>Max Drawdown:</strong> {metrics.maxDrawdown}</p>
            <p><strong>Accuracy:</strong> {metrics.accuracy * 100}%</p>
          </CardContent>
        </Card>
        
        {/* Right Card (Performance Chart) */}
        <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
          <CardContent className="space-y-5 py-6">
            <h3 className="text-xl font-semibold">Signal Performance vs Benchmark</h3>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={signalResults[0]?.performance.map((perf, idx) => ({ name: `Day ${idx + 1}`, SignalA: perf, Benchmark: 100 }))}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="SignalA" stroke="#8884d8" />
                <Line type="monotone" dataKey="Benchmark" stroke="#82ca9d" />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </motion.div>

      {/* Detailed Signal Table */}
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="mb-10"
      >
        <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
          <CardContent className="space-y-5 py-6">
            <h3 className="text-xl font-semibold">Discovered Signals</h3>
            <Table
              data={signalResults}
              columns={[
                { Header: "Signal Name", accessor: "name" },
                { Header: "Alpha", accessor: "alpha" },
                { Header: "Sharpe Ratio", accessor: "sharpeRatio" },
                { Header: "Max Drawdown", accessor: "maxDrawdown" },
                { Header: "Performance", accessor: "performance", Cell: ({ value }) => <span>{value.join(", ")}</span> },
              ]}
            />
          </CardContent>
        </Card>
      </motion.div>

      {/* CTA Button */}
      <motion.div
        className="text-center mt-12"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.6 }}
      >
        <Button className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
        onClick={() => navigate("/alpha-signals")}>
          Re-run Discovery
        </Button>
      </motion.div>
    </div>
  );
}
