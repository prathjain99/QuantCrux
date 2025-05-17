import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent } from "@/components/card";
import { Button } from "@/components/button";
import { motion } from "framer-motion";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import Table from "@/components/table";
import Header from "../../components/header";

export default function AlphaSignalResults() {
  const [signalResults, setSignalResults] = useState([]);
  const [metrics, setMetrics] = useState({
    alpha: 0,
    sharpeRatio: 0,
    maxDrawdown: 0,
    accuracy: 0,
    rSquared: 0,
    correlation: 0,
  });

  const navigate = useNavigate();

  const exampleResults = {
    signals: [
      {
        id: 1,
        name: "Signal A",
        alpha: 0.05,
        sharpeRatio: 1.2,
        maxDrawdown: 0.15,
        rSquared: 0.68,
        correlation: 0.72,
        direction: "Long",
        confidence: 0.89,
        performance: [20, 40, 60, 80, 100],
      },
      {
        id: 2,
        name: "Signal B",
        alpha: 0.03,
        sharpeRatio: 0.9,
        maxDrawdown: 0.18,
        rSquared: 0.55,
        correlation: 0.60,
        direction: "Short",
        confidence: 0.73,
        performance: [10, 30, 50, 70, 90],
      },
    ],
    metrics: {
      alpha: 0.04,
      sharpeRatio: 1.05,
      maxDrawdown: 0.16,
      accuracy: 0.75,
      rSquared: 0.65,
      correlation: 0.69,
    },
  };

  useEffect(() => {
    setSignalResults(exampleResults.signals);
    setMetrics(exampleResults.metrics);
  }, []);

  return (
    <div className="min-h-screen bg-[#0B1120] px-6 md:px-24 py-14 text-[#E2E8F0] font-sans">
      <Header showHome />
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="text-center mb-10"
      >
        <h1 className="text-4xl font-bold text-white mb-4">
          Alpha Signal Discovery Results
        </h1>
        <p className="text-gray-400 max-w-2xl mx-auto">
          Explore discovered alpha signals, predictive power, and performance metrics against benchmarks.
        </p>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-10"
      >
        <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
          <CardContent className="space-y-3 py-6">
            <h3 className="text-xl font-semibold">Performance Metrics</h3>
            <p><strong>Alpha:</strong> {metrics.alpha}</p>
            <p><strong>Sharpe Ratio:</strong> {metrics.sharpeRatio}</p>
            <p><strong>Max Drawdown:</strong> {metrics.maxDrawdown}</p>
            <p><strong>Accuracy:</strong> {(metrics.accuracy * 100).toFixed(2)}%</p>
            <p><strong>R² (Explained Variance):</strong> {metrics.rSquared}</p>
            <p><strong>Correlation:</strong> {metrics.correlation}</p>
          </CardContent>
        </Card>

        <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
          <CardContent className="space-y-5 py-6">
            <h3 className="text-xl font-semibold">Signal A Performance vs Benchmark</h3>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart
                data={signalResults[0]?.performance.map((perf, idx) => ({
                  name: `Day ${idx + 1}`,
                  SignalA: perf,
                  Benchmark: 100 + idx * 5,
                }))}
              >
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
                { Header: "R²", accessor: "rSquared" },
                { Header: "Correlation", accessor: "correlation" },
                { Header: "Direction", accessor: "direction" },
                { Header: "Confidence", accessor: "confidence", Cell: ({ value }) => `${(value * 100).toFixed(1)}%` },
                { Header: "Performance", accessor: "performance", Cell: ({ value }) => <span>{value.join(", ")}</span> },
              ]}
            />
          </CardContent>
        </Card>
      </motion.div>

      <motion.div
        className="text-center mt-12"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.6 }}
      >
        <Button
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
          onClick={() => navigate("/alpha-signals")}
        >
          Re-run Discovery
        </Button>
      </motion.div>
    </div>
  );
}
