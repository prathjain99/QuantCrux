// import React from "react";
// import { useLocation, useNavigate } from "react-router-dom";
// import { motion } from "framer-motion";
// import { Card, CardContent } from "@/components/card";
// import { Button } from "@/components/button";
// import Header from "@/components/header";

// export default function RiskAnalyticsResults() {
//   const { state } = useLocation();
//   const { riskMetrics, portfolioInfo } = state || {};
//   const navigate = useNavigate();

//   if (!riskMetrics) {
//     return (
//       <div className="min-h-screen bg-[#0B1120] text-white p-10">
//         <Header showHome />
//         <h2 className="text-xl">No risk metrics found. Please analyze risk first.</h2>
//       </div>
//     );
//   }

//   const {
//     volatility,
//     valueAtRisk,
//     conditionalVaR,
//     maxDrawdown,
//     beta,
//     sharpeRatio,
//     sortinoRatio,
//     correlationMatrix,
//   } = riskMetrics;

//   return (
//     <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14">
//       <Header showHome />

//       <motion.div
//         initial={{ opacity: 0, y: 30 }}
//         animate={{ opacity: 1, y: 0 }}
//         transition={{ duration: 0.6 }}
//         className="mb-10 text-center"
//       >
//         <h1 className="text-4xl font-bold mb-2">Risk Analytics Results</h1>
//         <p className="text-gray-400">Detailed risk metrics for your portfolio or custom inputs.</p>
//       </motion.div>

//       {/* Portfolio Info Summary */}
//       {portfolioInfo && (
//         <Card className="bg-[#1C2433] rounded-2xl mb-8">
//           <CardContent className="p-6">
//             <h2 className="text-xl font-semibold mb-4">Portfolio Summary</h2>
//             <ul className="space-y-2 text-gray-300">
//               <li>🧾 Asset Count: {portfolioInfo.assetCount}</li>
//               <li>📈 Portfolio Type: {portfolioInfo.source === "optimizer" ? "From Optimizer" : "Manual Input"}</li>
//             </ul>
//           </CardContent>
//         </Card>
//       )}

//       {/* Risk Metrics */}
//       <Card className="bg-[#1C2433] rounded-2xl mb-8">
//         <CardContent className="p-6">
//           <h2 className="text-xl font-semibold mb-4">Key Risk Metrics</h2>
//           <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
//             <div className="bg-[#2A3548] p-4 rounded-lg">
//               Volatility: <span className="text-yellow-400">{volatility}%</span>
//             </div>
//             <div className="bg-[#2A3548] p-4 rounded-lg">
//               VaR (95%): <span className="text-red-400">{valueAtRisk}%</span>
//             </div>
//             <div className="bg-[#2A3548] p-4 rounded-lg">
//               CVaR (95%): <span className="text-orange-400">{conditionalVaR}%</span>
//             </div>
//             <div className="bg-[#2A3548] p-4 rounded-lg">
//               Max Drawdown: <span className="text-red-400">{maxDrawdown}%</span>
//             </div>
//             <div className="bg-[#2A3548] p-4 rounded-lg">
//               Beta: <span className="text-purple-400">{beta}</span>
//             </div>
//             <div className="bg-[#2A3548] p-4 rounded-lg">
//               Sharpe Ratio: <span className="text-blue-400">{sharpeRatio}</span>
//             </div>
//             <div className="bg-[#2A3548] p-4 rounded-lg">
//               Sortino Ratio: <span className="text-green-400">{sortinoRatio}</span>
//             </div>
//           </div>
//         </CardContent>
//       </Card>

//       {/* Correlation Matrix - Placeholder (actual chart to be added later) */}
//       <Card className="bg-[#1C2433] rounded-2xl mb-8">
//         <CardContent className="p-6">
//           <h2 className="text-xl font-semibold mb-4">Correlation Matrix</h2>
//           <div className="bg-[#2A3548] text-center text-gray-400 p-10 rounded-xl">
//             📊 Heatmap or table visualization will go here
//           </div>
//         </CardContent>
//       </Card>

//       {/* Back or CTA Button */}
//       <div className="text-center mt-10">
//         <Button
//           onClick={() => navigate("/risk-analytics")}
//           className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
//         >
//           Back to Risk Analysis 🔁
//         </Button>
//       </div>
//     </div>
//   );
// }


import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Card, CardContent } from "@/components/card";
import { Button } from "@/components/button";
import Header from "@/components/header";

export default function RiskAnalyticsResults() {
  const { state } = useLocation();
  const navigate = useNavigate();

  // Dummy fallback data
  const dummyRiskMetrics = {
    volatility: 12.4,
    valueAtRisk: 6.2,
    conditionalVaR: 8.1,
    maxDrawdown: 15.3,
    beta: 0.87,
    sharpeRatio: 1.25,
    sortinoRatio: 1.45,
    correlationMatrix: [],
  };

  const dummyPortfolioInfo = {
    assetCount: 6,
    source: "optimizer",
  };

  const { riskMetrics = dummyRiskMetrics, portfolioInfo = dummyPortfolioInfo } = state || {};

  return (
    <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14">
      <Header showHome />
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="mb-10 text-center"
      >
        <h1 className="text-4xl font-bold mb-2">Risk Analytics Report</h1>
        <p className="text-gray-400">Comprehensive risk profile of your portfolio.</p>
      </motion.div>

      {/* Portfolio Info Summary */}
      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6">
          <h2 className="text-xl font-semibold mb-4">Portfolio Info</h2>
          <ul className="space-y-2 text-gray-300">
            <li>📦 Total Assets: {portfolioInfo.assetCount}</li>
            <li>🔍 Source: {portfolioInfo.source === "optimizer" ? "Portfolio Optimizer" : "Manual Input"}</li>
          </ul>
        </CardContent>
      </Card>

      {/* Risk Metrics */}
      <Card className="bg-[#1C2433] rounded-2xl mb-8">
        <CardContent className="p-6">
          <h2 className="text-xl font-semibold mb-4">Risk Metrics</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Volatility: <span className="text-yellow-400">{riskMetrics.volatility}%</span>
            </div>
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Value at Risk (VaR): <span className="text-red-400">{riskMetrics.valueAtRisk}%</span>
            </div>
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Conditional VaR: <span className="text-orange-400">{riskMetrics.conditionalVaR}%</span>
            </div>
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Max Drawdown: <span className="text-red-500">{riskMetrics.maxDrawdown}%</span>
            </div>
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Beta: <span className="text-blue-400">{riskMetrics.beta}</span>
            </div>
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Sharpe Ratio: <span className="text-green-400">{riskMetrics.sharpeRatio}</span>
            </div>
            <div className="bg-[#2A3548] p-4 rounded-lg">
              Sortino Ratio: <span className="text-purple-400">{riskMetrics.sortinoRatio}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Back to Risk Analyzer */}
      <motion.div
        className="text-center mt-12"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.6 }}
      >
        <Button
          onClick={() => navigate("/risk-analytics")}
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
        >
          Back to Risk Analyzer
        </Button>
      </motion.div>
    </div>
  );
}
