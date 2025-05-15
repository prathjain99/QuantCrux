
import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Card, CardContent } from "@/components/card";
import { Button } from "@/components/button";
import Header from "@/components/header";

export default function PortfolioOptimizerResults() {
  const navigate = useNavigate();
  const { state } = useLocation();

  // Dummy data fallback if no state passed
  const dummyFormData = {
    objectiveFunction: "Maximize Sharpe Ratio",
    optimizationMethod: "Mean-Variance",
    riskFreeRate: 6,
    maxWeight: 20,
    allowShortSelling: false,
    includeTransactionCosts: true,
    transactionCostRate: 0.1,
  };

  const dummyResults = {
    optimizedWeights: [
      { ticker: "RELIANCE", weight: 18.23 },
      { ticker: "TCS", weight: 15.12 },
      { ticker: "HDFCBANK", weight: 12.34 },
      { ticker: "INFY", weight: 10.5 },
      { ticker: "ICICIBANK", weight: 8.6 },
      { ticker: "KOTAKBANK", weight: 7.1 },
      { ticker: "LT", weight: 6.9 },
      { ticker: "HINDUNILVR", weight: 5.7 },
      { ticker: "ITC", weight: 5.2 },
      { ticker: "SBIN", weight: 4.3 },
    ],
    metrics: {
      expectedReturn: 14.56,
      volatility: 8.23,
      sharpeRatio: 1.22,
      maxDrawdown: 10.5,
    },
  };

  const formData = state?.formData || dummyFormData;
  const results = state?.results || dummyResults;

  const { optimizedWeights, metrics } = results;

  const handleOptimizeResults = () => {
    navigate("/portfolio");
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
        <h1 className="text-4xl font-bold mb-2">Portfolio Optimization Results</h1>
        <p className="text-gray-400">
          Here is your optimized portfolio based on your inputs and constraints.
        </p>
      </motion.div>

      {/* Summary Section */}
      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6">
          <h2 className="text-xl font-semibold mb-4">Optimization Summary</h2>
          <ul className="space-y-2 text-gray-300">
            <li>🎯 Objective: {formData.objectiveFunction}</li>
            <li>📊 Method: {formData.optimizationMethod}</li>
            <li>📈 Risk-Free Rate: {formData.riskFreeRate}%</li>
            <li>
              🔒 Constraints: Max Weight {formData.maxWeight}%,{" "}
              {formData.allowShortSelling ? "Short Selling Allowed" : "Long-only"}
            </li>
            {formData.includeTransactionCosts && (
              <li>💸 Transaction Cost Rate: {formData.transactionCostRate}%</li>
            )}
          </ul>
        </CardContent>
      </Card>

      {/* Optimized Weights */}
      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6">
          <h2 className="text-xl font-semibold mb-4">Optimized Portfolio Weights</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            {optimizedWeights.map((asset, i) => (
              <div
                key={i}
                className="bg-[#2A3548] p-3 rounded-lg text-center text-white"
              >
                <div className="font-medium">{asset.ticker}</div>
                <div className="text-blue-400">{asset.weight.toFixed(2)}%</div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Portfolio Metrics */}
      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6">
          <h2 className="text-xl font-semibold mb-4">Portfolio Metrics</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Expected Return:{" "}
              <span className="text-green-400">{metrics.expectedReturn}%</span>
            </div>
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Volatility: <span className="text-yellow-400">{metrics.volatility}%</span>
            </div>
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Sharpe Ratio: <span className="text-blue-400">{metrics.sharpeRatio}</span>
            </div>
            {metrics.maxDrawdown && (
              <div className="bg-[#2A3548] p-4 rounded-lg">
                Max Drawdown: <span className="text-red-400">{metrics.maxDrawdown}%</span>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Download Report Button */}
      <div className="text-center mt-8">
        <Button
          onClick={() => window.print()}
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-4 py-2 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
        >
          Download Report 📄
        </Button>
      </div>

      {/* Back to Optimizer Button */}
      <motion.div
        className="text-center mt-12"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.6 }}
      >
        <Button
          onClick={handleOptimizeResults}
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
        >
          Re-Run Optimization🚀
        </Button>
      </motion.div>
    </div>
  );
}
