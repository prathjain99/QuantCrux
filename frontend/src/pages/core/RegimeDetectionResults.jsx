import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Card, CardContent } from "@/components/card";
import Header from "@/components/header";
import { Button } from "@/components/button";

export default function RegimeDetectionResults() {
  const location = useLocation();
   const navigate = useNavigate();
  const formData = location.state?.formData || {};

  return (
    <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14">
      <Header showHome />

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="mb-10 text-center"
      >
        <h1 className="text-4xl font-bold mb-2">Market Regime Detection Results</h1>
        <p className="text-gray-400 max-w-2xl mx-auto">
          Below are the summary of detected regimes and transition points based on your input configuration.
        </p>
      </motion.div>

      {/* Input Summary */}
      <Card className="bg-[#1C2433] rounded-2xl mb-10">
        <CardContent className="p-6">
          <h2 className="text-2xl font-semibold mb-4">Input Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm text-gray-300">
            <p><strong>Asset Tickers:</strong> {formData.assetTickers || "From Optimizer"}</p>
            <p><strong>Frequency:</strong> {formData.frequency}</p>
            <p><strong>Lookback Period:</strong> {formData.lookback}</p>
            <p><strong>Detection Method:</strong> {formData.method.toUpperCase()}</p>
            <p><strong>Rolling Window:</strong> {formData.rollingWindow}</p>
            {formData.method !== "non-ml" && (
              <p><strong>Number of Regimes:</strong> {formData.numRegimes}</p>
            )}
            <p><strong>Used Optimizer Output:</strong> {formData.useOptimizerOutput ? "Yes" : "No"}</p>
          </div>
        </CardContent>
      </Card>

      {/* Regime Results Summary */}
      <Card className="bg-[#1C2433] rounded-2xl mb-10">
        <CardContent className="p-6">
          <h2 className="text-2xl font-semibold mb-4">Detected Regime Summary</h2>
          <ul className="list-disc pl-5 text-gray-300 space-y-2">
            <li>Regime 1 (Stable): Detected from Jan 2020 to Feb 2021</li>
            <li>Regime 2 (Volatile): Detected from Mar 2021 to Nov 2022</li>
            <li>Regime 3 (Recovery): Detected from Dec 2022 onwards</li>
          </ul>
        </CardContent>
      </Card>

      {/* Charts Placeholder */}
      <Card className="bg-[#1C2433] rounded-2xl mb-10">
        <CardContent className="p-6">
          <h2 className="text-2xl font-semibold mb-4">Visualization (Coming Soon)</h2>
          <div className="text-center text-gray-400 italic">
            Regime overlay plot and volatility clustering charts will appear here.
          </div>
        </CardContent>
      </Card>

      {/* Download Placeholder */}
      
       <div className="text-center mt-10">
                   <Button
                     onClick={() => navigate("/regime-detection")}
                     className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform"
                   >
                    Back to Detection 📊
                   </Button>
        </div>
      
    </div>
  );
}
