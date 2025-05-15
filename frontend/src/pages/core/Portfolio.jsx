

import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Input from "@/components/input";
import { Button } from "@/components/button";
import { Card, CardContent } from "@/components/card";
import { motion } from "framer-motion";
import Header from "../../components/header";

export default function PortfolioOptimizer() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    assetList: "",
    riskFreeRate: 6,
    allowShortSelling: false,
    maxAssetWeight: 100,
    includeTransactionCosts: true,
    transactionCostRate: 0.1,
    objectiveFunction: "Maximize Sharpe Ratio",
    optimizationType: "Mean-Variance",
  });

  const objectiveOptions = [
    "Maximize Sharpe Ratio",
    "Minimize Volatility",
    "Maximize Return",
  ];

  const optimizationMethods = [
    "Mean-Variance",
    "Black-Litterman",
    "Risk Parity",
  ];

  const handleChange = (e) => {
    const { name, type, value, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleOptimize = () => {
    navigate("/portfolio-results", { state: { formData: form } });
  };

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
          Portfolio Optimizer
        </h1>
        <p className="text-gray-400 max-w-2xl mx-auto">
          Customize your portfolio preferences and let our engine optimize it based on risk, return, and constraints.
        </p>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* LEFT CARD */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2, duration: 0.6 }}
        >
          <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
            <CardContent className="space-y-5 py-6">
              <Input
                label="Asset List"
                name="assetList"
                value={form.assetList}
                onChange={handleChange}
                placeholder="e.g., TCS, INFY, HDFCBANK"
              />
              <Input
                type="number"
                label="Risk-Free Rate (%)"
                name="riskFreeRate"
                value={form.riskFreeRate}
                onChange={handleChange}
              />
              <Input
                type="number"
                label="Max Weight per Asset (%)"
                name="maxAssetWeight"
                value={form.maxAssetWeight}
                onChange={handleChange}
              />
              <div className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  id="allowShortSelling"
                  name="allowShortSelling"
                  checked={form.allowShortSelling}
                  onChange={handleChange}
                  className="accent-blue-600"
                />
                <label htmlFor="allowShortSelling" className="text-sm font-medium text-white">
                  Allow Short Selling
                </label>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* RIGHT CARD */}
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.4, duration: 0.6 }}
        >
          <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
            <CardContent className="space-y-5 py-6">
              <div>
                <label className="block mb-1 text-sm font-medium text-white">
                  Objective Function
                </label>
                <select
                  name="objectiveFunction"
                  value={form.objectiveFunction}
                  onChange={handleChange}
                  className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md"
                >
                  {objectiveOptions.map((opt, i) => (
                    <option key={i} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-white">
                  Optimization Method
                </label>
                <select
                  name="optimizationType"
                  value={form.optimizationType}
                  onChange={handleChange}
                  className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md"
                >
                  {optimizationMethods.map((opt, i) => (
                    <option key={i} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  id="includeTransactionCosts"
                  name="includeTransactionCosts"
                  checked={form.includeTransactionCosts}
                  onChange={handleChange}
                  className="accent-blue-600"
                />
                <label htmlFor="includeTransactionCosts" className="text-sm font-medium text-white">
                  Include Transaction Costs
                </label>
              </div>
              {form.includeTransactionCosts && (
                <Input
                  type="number"
                  label="Transaction Cost Rate (%)"
                  name="transactionCostRate"
                  value={form.transactionCostRate}
                  onChange={handleChange}
                />
              )}
            </CardContent>
          </Card>
        </motion.div>
      </div>

      {/* CTA Button */}
      <motion.div
        className="text-center mt-12"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.6 }}
      >
        <Button
          onClick={handleOptimize}
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
        >
          Optimize Portfolio 🚀
        </Button>
      </motion.div>
    </div>
  );
}
