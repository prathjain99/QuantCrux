import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import Input from "@/components/input";
import { Button } from "@/components/button";
import { Card, CardContent } from "@/components/card";
import Header from "@/components/header";

export default function RiskAnalytics() {
  const navigate = useNavigate();

  const [usePortfolioOutput, setUsePortfolioOutput] = useState(false);
  const [form, setForm] = useState({
    tickers: "",
    weights: "",
    riskFreeRate: 6,
    confidenceLevel: 95,
    timeHorizon: 1,
    benchmark: "NIFTY100",
    startDate: "2020-01-01",
    endDate: "2024-12-31",
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({
      ...form,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const handleSubmit = () => {
    navigate("/risk-analytics-results", {
      state: {
        inputMode: usePortfolioOutput ? "portfolio" : "manual",
        formData: form,
      },
    });
  };

  return (
    <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14">
      <Header showHome />

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="mb-10 text-center"
      >
        <h1 className="text-4xl font-bold mb-2">Risk Analytics Module</h1>
        <p className="text-gray-400">
          Analyze risk metrics like volatility, VaR, drawdown, and more for your portfolio.
        </p>
      </motion.div>

      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6 space-y-6">
          <div className="flex items-center space-x-3">
            <input
              type="checkbox"
              id="usePortfolioOutput"
              name="usePortfolioOutput"
              checked={usePortfolioOutput}
              onChange={() => setUsePortfolioOutput(!usePortfolioOutput)}
              className="accent-blue-600"
            />
            <label htmlFor="usePortfolioOutput" className="text-sm font-medium text-white">
              Use Portfolio Optimizer Output
            </label>
          </div>

          {!usePortfolioOutput && (
            <>
              <Input
                label="Asset Tickers (comma-separated)"
                name="tickers"
                value={form.tickers}
                onChange={handleChange}
                placeholder="e.g., TCS.NS, INFY.NS, HDFCBANK.NS"
              />
              <Input
                label="Portfolio Weights (comma-separated, in %)"
                name="weights"
                value={form.weights}
                onChange={handleChange}
                placeholder="e.g., 30, 40, 30"
              />
            </>
          )}

          <div className="grid md:grid-cols-2 gap-4">
            <Input
              type="number"
              label="Risk-Free Rate (%)"
              name="riskFreeRate"
              value={form.riskFreeRate}
              onChange={handleChange}
            />
            <Input
              type="number"
              label="Confidence Level for VaR (%)"
              name="confidenceLevel"
              value={form.confidenceLevel}
              onChange={handleChange}
            />
            <Input
              type="number"
              label="Time Horizon for VaR (days)"
              name="timeHorizon"
              value={form.timeHorizon}
              onChange={handleChange}
            />
            <Input
              label="Benchmark Index"
              name="benchmark"
              value={form.benchmark}
              onChange={handleChange}
              placeholder="e.g., NIFTY100"
            />
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            <Input
              type="date"
              label="Start Date"
              name="startDate"
              value={form.startDate}
              onChange={handleChange}
            />
            <Input
              type="date"
              label="End Date"
              name="endDate"
              value={form.endDate}
              onChange={handleChange}
            />
          </div>

          <div className="text-center pt-6">
            <Button
              onClick={handleSubmit}
              className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
            >
              Calculate Risk Metrics ⚠️
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
