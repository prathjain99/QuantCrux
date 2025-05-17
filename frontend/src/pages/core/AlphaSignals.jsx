import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Input from "@/components/input";
import { Button } from "@/components/button";
import { Card, CardContent } from "@/components/card";
import { motion } from "framer-motion";
import Header from "@/components/header";
import { Info } from "lucide-react";

export default function AlphaSignal() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    assetUniverse: "",
    startDate: "",
    endDate: "",
    predictionHorizon: 5,
    targetVariable: "Next Day Return",
    features: [],
    modelType: "XGBoost",
    preprocessing: "StandardScaler",
    taskType: "Regression",
  });

  const featureOptions = [
    "Technical Indicators",
    "Price Action",
    "Macro Indicators",
    "Volume & Volatility",
    "Momentum",
    "Rolling Statistics",
  ];

  const modelOptions = [
    "XGBoost",
    "LightGBM",
    "CatBoost",
    "Neural Network",
  ];

  const targetOptions = [
    "Next Day Return",
    "5-Day Return",
    "10-Day Return",
    "Volatility",
    "Sharpe Ratio",
    "Sortino Ratio",
    "Direction (Up/Down)",
  ];

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    if (type === "checkbox") {
      setForm((prev) => ({
        ...prev,
        features: checked
          ? [...prev.features, value]
          : prev.features.filter((f) => f !== value),
      }));
    } else {
      setForm((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleDiscoverAlphaSignals = () => {
    navigate("/alpha-signals-results", { state: { formData: form } });
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
          Alpha Signal Discovery
        </h1>
        <p className="text-gray-400 max-w-2xl mx-auto">
          Discover alpha-generating signals from a rich feature set using advanced ML models.
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
                label="Asset Universe"
                name="assetUniverse"
                value={form.assetUniverse}
                onChange={handleChange}
                placeholder="e.g., NIFTY100"
              />
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
              <Input
                type="number"
                label="Prediction Horizon (days)"
                name="predictionHorizon"
                min={1}
                value={form.predictionHorizon}
                onChange={handleChange}
              />
              <div>
                <label className="block mb-1 text-sm font-medium text-white flex items-center gap-2">
                  Target Variable
                  <Info size={16} className="text-gray-400" title="Choose your prediction target" />
                </label>
                <select
                  name="targetVariable"
                  value={form.targetVariable}
                  onChange={handleChange}
                  className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md"
                >
                  {targetOptions.map((target, i) => (
                    <option key={i} value={target}>
                      {target}
                    </option>
                  ))}
                </select>
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
            <CardContent className="space-y-6 py-6">
              <div>
                <label className="block mb-1 text-sm font-medium text-white flex items-center gap-2">
                  Feature Set
                  <Info size={16} className="text-gray-400" title="Select features for model training" />
                </label>
                <div className="grid grid-cols-2 gap-3">
                  {featureOptions.map((feat, i) => (
                    <label key={i} className="flex items-center space-x-2 text-sm">
                      <input
                        type="checkbox"
                        value={feat}
                        checked={form.features.includes(feat)}
                        onChange={handleChange}
                        className="accent-blue-600"
                      />
                      <span>{feat}</span>
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <label className="block mb-1 text-sm font-medium text-white">
                  Model Type
                </label>
                <select
                  name="modelType"
                  value={form.modelType}
                  onChange={handleChange}
                  className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md"
                >
                  {modelOptions.map((model, i) => (
                    <option key={i} value={model}>
                      {model}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block mb-1 text-sm font-medium text-white">
                  Preprocessing
                </label>
                <select
                  name="preprocessing"
                  value={form.preprocessing}
                  onChange={handleChange}
                  className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md"
                >
                  <option value="StandardScaler">StandardScaler</option>
                  <option value="MinMaxScaler">MinMaxScaler</option>
                  <option value="Normalizer">Normalizer</option>
                </select>
              </div>

              <div>
                <label className="block mb-1 text-sm font-medium text-white">
                  Task Type
                </label>
                <select
                  name="taskType"
                  value={form.taskType}
                  onChange={handleChange}
                  className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md"
                >
                  <option value="Regression">Regression</option>
                  <option value="Classification">Classification</option>
                </select>
              </div>
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
          onClick={handleDiscoverAlphaSignals}
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
        >
          Discover Alpha Signals 🚀
        </Button>
      </motion.div>
    </div>
  );
}
