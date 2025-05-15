import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Card, CardContent } from "@/components/card";
import { Button } from "@/components/button";
import Header from "@/components/header";

export default function OptionsPricingResults() {
  const { state } = useLocation();
  const navigate = useNavigate();

  // Dummy data for testing
  const dummyResults = {
    optionPrice: 12.45,
    method: "Black-Scholes",
    greeks: {
      delta: 0.62,
      gamma: 0.042,
      theta: -0.017,
      vega: 0.13,
      rho: 0.18,
    },
    payoffData: [], // for chart later
  };

  const formData = state?.formData || {
    optionType: "Call",
    exerciseType: "European",
    underlyingPrice: 100,
    strikePrice: 95,
    timeToMaturity: 0.5,
    riskFreeRate: 5,
    volatility: 20,
    dividendYield: 0,
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
        <h1 className="text-4xl font-bold mb-2">Option Pricing Results</h1>
        <p className="text-gray-400">
          Here is the calculated option price and sensitivities.
        </p>
      </motion.div>

      {/* Summary Section */}
      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6">
          <h2 className="text-xl font-semibold mb-4">Input Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-y-2 gap-x-6 text-gray-300 text-sm">
            <div>📈 Option Type: {formData.optionType}</div>
            <div>🔁 Exercise Type: {formData.exerciseType}</div>
            <div>💹 Underlying Price: ₹{formData.underlyingPrice}</div>
            <div>🎯 Strike Price: ₹{formData.strikePrice}</div>
            <div>⏳ Time to Maturity: {formData.timeToMaturity} years</div>
            <div>🏦 Risk-Free Rate: {formData.riskFreeRate}%</div>
            <div>📉 Volatility: {formData.volatility}%</div>
            <div>💰 Dividend Yield: {formData.dividendYield}%</div>
            </div>
        </CardContent>
      </Card>

      {/* Option Price */}
      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6 text-center">
          <h2 className="text-xl font-semibold mb-2">Option Price</h2>
          <p className="text-3xl text-green-400 font-bold">₹{dummyResults.optionPrice}</p>
          <p className="text-sm text-gray-400 mt-1">Calculated using {dummyResults.method}</p>
        </CardContent>
      </Card>

      {/* Greeks */}
      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6">
          <h2 className="text-xl font-semibold mb-4">Option Greeks</h2>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4 text-sm">
            {Object.entries(dummyResults.greeks).map(([greek, value], idx) => (
              <div
                key={idx}
                className="bg-[#2A3548] p-4 rounded-lg text-center"
              >
                <div className="font-medium capitalize">{greek}</div>
                <div className="text-blue-400 text-lg font-semibold">{value}</div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* CTA Buttons */}
        <div className="text-center mt-12 space-x-4">
        <Button
          onClick={() => window.print()}
          className="bg-blue-700 text-white px-6 py-1 rounded-lg"
        >
          Download Report 📄
        </Button>
      </div>
      <div className="text-center mt-12 space-x-4">
        <Button
          onClick={() => navigate("/options-pricing")}
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform"
        >
          Recalculate 🔁
        </Button>
        </div>

    </div>
  );
}
