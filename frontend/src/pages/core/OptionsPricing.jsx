import React, { useState } from "react";
import { motion } from "framer-motion";
import Input from "@/components/input";
import { Button } from "@/components/button";
import Header from "@/components/header";
import { useNavigate } from "react-router-dom";

export default function OptionsPricing() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    optionType: "call",
    exerciseType: "European",
    underlyingPrice: "",
    strikePrice: "",
    timeToMaturity: "",
    riskFreeRate: "",
    volatility: "",
    dividendYield: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = () => {
    // For now, use dummy output. Real model will be plugged in later.
    const dummyResult = {
      price: 12.45,
      greeks: {
        delta: 0.55,
        gamma: 0.04,
        theta: -0.02,
        vega: 0.10,
        rho: 0.03,
      },
    };
    navigate("/options-pricing-results", { state: { formData: form, results: dummyResult } });
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
        <h1 className="text-4xl font-bold mb-2">Options & Derivatives Pricing</h1>
        <p className="text-gray-400">
          Calculate fair option prices and Greeks using Black-Scholes, Binomial Tree, or Monte Carlo.
        </p>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block mb-1 text-sm">Option Type</label>
          <select
            name="optionType"
            value={form.optionType}
            onChange={handleChange}
            className="bg-[#1C2433] border border-gray-600 rounded-lg px-3 py-2 w-full"
          >
            <option value="call">Call</option>
            <option value="put">Put</option>
          </select>
        </div>

        <div>
          <label className="block mb-1 text-sm">Exercise Type</label>
          <select
            name="exerciseType"
            value={form.exerciseType}
            onChange={handleChange}
            className="bg-[#1C2433] border border-gray-600 rounded-lg px-3 py-2 w-full"
          >
            <option value="European">European</option>
            <option value="American">American</option>
            <option value="Asian">Asian</option>
            <option value="Barrier">Barrier</option>
          </select>
        </div>

        <Input
          label="Underlying Price (S)"
          type="number"
          name="underlyingPrice"
          value={form.underlyingPrice}
          onChange={handleChange}
        />

        <Input
          label="Strike Price (K)"
          type="number"
          name="strikePrice"
          value={form.strikePrice}
          onChange={handleChange}
        />

        <Input
          label="Time to Maturity (Years)"
          type="number"
          name="timeToMaturity"
          value={form.timeToMaturity}
          onChange={handleChange}
        />

        <Input
          label="Risk-Free Rate (%)"
          type="number"
          name="riskFreeRate"
          value={form.riskFreeRate}
          onChange={handleChange}
        />

        <Input
          label="Volatility (%)"
          type="number"
          name="volatility"
          value={form.volatility}
          onChange={handleChange}
        />

        <Input
          label="Dividend Yield (%) (optional)"
          type="number"
          name="dividendYield"
          value={form.dividendYield}
          onChange={handleChange}
        />
      </div>

      <div className="text-center mt-10">
        <Button
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
          onClick={handleSubmit}
        >
          Calculate Option Price ⚙️
        </Button>
      </div>
    </div>
  );
}
