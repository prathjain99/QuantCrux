
// import React, { useState } from "react";
// import Header from "@/components/header";
// import { motion } from "framer-motion";
// import { Button } from "@/components/button";
// import Slider from "@/components/slider";
// import Input from "@/components/input";
// import { Card, CardContent } from "@/components/card";

// import { useNavigate } from "react-router-dom";

// export default function Backtesting() {
//   const [form, setForm] = useState({
//     strategyType: "Momentum",
//     assetUniverse: "Single Asset",
//     assetSymbol: "AAPL",
//     timeframe: "Daily",
//     fromDate: "2018-01-01",
//     toDate: "2024-01-01",
//     lookback: 20,
//     entryCondition: "",
//     exitCondition: "",
//     stopLoss: 2,
//     takeProfit: 5,
//     positionSizing: "Fixed",
//     slippage: 0.1,
//     transactionCost: 0.05,
//     capital: 100000,
//     leverage: 1,
//   });

//   const handleChange = (e) => {
//     const { name, value } = e.target;
//     setForm((prev) => ({ ...prev, [name]: value }));
//   };

//     const navigate = useNavigate();

//   const handleRunBacktest = () => {
//     console.log("Running backtest with configuration:", form);
//     navigate("/backtest-result");
//   };

  

//   return (
//     <div className="bg-[#0B1120] min-h-screen px-6 py-10 text-gray-100">
//         <Header showHome />
//       <motion.h1
//         className="text-4xl font-bold text-center mb-10 text-white"
//         initial={{ opacity: 0, y: -20 }}
//         animate={{ opacity: 1, y: 0 }}
//         transition={{ duration: 0.6 }}
//       >
//         Configure Strategy Backtest
//       </motion.h1>

//       {/* <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8"> */}
//       <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8">
//               {/* LEFT CARD */}
//               <motion.div
//                 initial={{ opacity: 0, x: -20 }}
//                 animate={{ opacity: 1, x: 0 }}
//                 transition={{ delay: 0.2, duration: 0.6 }}
//               >

//         {/* Strategy Configuration */}
//         <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
//             <CardContent className="space-y-5 py-6">
//         <div className="space-y-6">
//           <h2 className="text-xl font-semibold mb-2">Strategy Type</h2>
//           <select name="strategyType" value={form.strategyType} onChange={handleChange} className="w-full px-4 py-2 rounded-md bg-[#1C2433]">
//             <option>Momentum</option>
//             <option>Mean Reversion</option>
//             <option>Pairs Trading</option>
//             <option>Custom Rule-Based</option>
//           </select>

//           <h2 className="text-xl font-semibold mt-6 mb-2">Asset Universe</h2>
//           <select name="assetUniverse" value={form.assetUniverse} onChange={handleChange} className="w-full px-4 py-2 rounded-md bg-[#1C2433]">
//             <option>Single Asset</option>
//             <option>Custom Basket</option>
//             <option>ETF or Sector</option>
//           </select>

//           <Input
//             label="Asset Symbol / Basket"
//             name="assetSymbol"
//             value={form.assetSymbol}
//             onChange={handleChange}
//             placeholder="e.g., AAPL or NIFTY100"
//           />

//           <h2 className="text-xl font-semibold mt-6 mb-2">Timeframe & Dates</h2>
//           <select name="timeframe" value={form.timeframe} onChange={handleChange} className="w-full px-4 py-2 rounded-md bg-[#1C2433]">
//             <option>1m</option>
//             <option>5m</option>
//             <option>15m</option>
//             <option>Daily</option>
//             <option>Weekly</option>
//           </select>

//           <div className="flex gap-4 mt-2">
//             <Input label="From" type="date" name="fromDate" value={form.fromDate} onChange={handleChange} />
//             <Input label="To" type="date" name="toDate" value={form.toDate} onChange={handleChange} />
//           </div>
//         </div>
//          </CardContent>
//                   </Card>
//                   </motion.div>
        
        
 
//               <motion.div
//                 initial={{ opacity: 0, x: -20 }}
//                 animate={{ opacity: 1, x: 0 }}
//                 transition={{ delay: 0.2, duration: 0.6 }}
//               >
//       <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
//             <CardContent className="space-y-5 py-6">
//         <div className="space-y-6">
//     <h2 className="text-xl font-semibold">Strategy Parameters</h2>
//     <Slider label="Lookback Period" min={5} max={100} step={1} value={form.lookback} onChange={(v) => setForm({ ...form, lookback: v })} />
//     <Input label="Entry Condition" name="entryCondition" value={form.entryCondition} onChange={handleChange} placeholder="e.g., SMA50 > SMA200" />
//     <Input label="Exit Condition" name="exitCondition" value={form.exitCondition} onChange={handleChange} placeholder="e.g., SMA50 < SMA200" />
//     <Slider label="Stop-Loss (%)" min={0} max={20} step={0.5} value={form.stopLoss} onChange={(v) => setForm({ ...form, stopLoss: v })} />
//     <Slider label="Take-Profit (%)" min={0} max={50} step={0.5} value={form.takeProfit} onChange={(v) => setForm({ ...form, takeProfit: v })} />

//   <div className="space-y-2">
//     <label htmlFor="positionSizing" className="block text-sm font-medium text-gray-300">Position Sizing</label>
//     <select
//       id="positionSizing"
//       name="positionSizing"
//       value={form.positionSizing}
//       onChange={handleChange}
//       className="w-full px-4 py-2 rounded-md bg-[#1C2433] text-white"
//     >
//       <option>Fixed</option>
//       <option>Percentage of Capital</option>
//       <option>Volatility-Based</option>
//       <option>Kelly Criterion</option>
//     </select>
//   </div>
// </div>
// </CardContent>
// </Card>
// </motion.div>
// </div>

// <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid gap-8 py-6">
//  <motion.div
//                 initial={{ opacity: 0, x: -20 }}
//                 animate={{ opacity: 1, x: 0 }}
//                 transition={{ delay: 0.2, duration: 0.6 }}
//               >
// <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
//             <CardContent className="space-y-5 py-6">
//         {/* Costs & Capital */}
//         <div className="col-span-1 md:col-span-2 mt-10 space-y-4">
//           <h2 className="text-xl font-semibold">Capital & Costs</h2>
//           <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
//             <Slider label="Initial Capital (₹)" min={10000} max={1000000} step={10000} value={form.capital} onChange={(v) => setForm({ ...form, capital: v })} />
//             <Slider label="Leverage (x)" min={1} max={10} step={1} value={form.leverage} onChange={(v) => setForm({ ...form, leverage: v })} />
//             <Slider label="Slippage (%)" min={0} max={1} step={0.01} value={form.slippage} onChange={(v) => setForm({ ...form, slippage: v })} />
//             <Slider label="Transaction Cost (%)" min={0} max={1} step={0.01} value={form.transactionCost} onChange={(v) => setForm({ ...form, transactionCost: v })} />
//           </div>
//         </div>
//         </CardContent>
//         </Card>
//         </motion.div>
//       </div>

     
//           <motion.div
//             className="text-center mt-12"
//             initial={{ opacity: 0, scale: 0.9 }}
//             animate={{ opacity: 1, scale: 1 }}
//             transition={{ delay: 0.6 }}
//           >
//             <Button
//              onClick={handleRunBacktest}
//              className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg">
//               Run Backtest 🚀
//             </Button>
//           </motion.div>

//     </div>
//   );
// }


import React, { useState } from "react";
import Header from "@/components/header";
import { motion } from "framer-motion";
import { Button } from "@/components/button";
import Slider from "@/components/slider";
import Input from "@/components/input";
import { Card, CardContent } from "@/components/card";
import { useNavigate } from "react-router-dom";

export default function Backtesting() {
  const [form, setForm] = useState({
    strategyType: "Momentum",
    assetUniverse: "Single Asset",
    assetSymbol: "AAPL",
    timeframe: "Daily",
    fromDate: "2018-01-01",
    toDate: "2024-01-01",
    lookback: 20,
    entryCondition: "",
    exitCondition: "",
    stopLoss: 2,
    takeProfit: 5,
    positionSizing: "Fixed",
    slippage: 0.1,
    transactionCost: 0.05,
    capital: 100000,
    leverage: 1,
    rebalanceFrequency: "None",
    maxDrawdownLimit: 0,
    maxPositionSize: 100,
    benchmark: "NIFTY100"
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const navigate = useNavigate();

  const handleRunBacktest = () => {
    console.log("Running backtest with configuration:", form);
    navigate("/backtest-result");
  };

  return (
    <div className="bg-[#0B1120] min-h-screen px-6 py-10 text-gray-100">
      <Header showHome />
      <motion.h1
        className="text-4xl font-bold text-center mb-10 text-white"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        Configure Strategy Backtest
      </motion.h1>

      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* LEFT CARD */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2, duration: 0.6 }}
        >
          {/* Strategy Configuration */}
          <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
            <CardContent className="space-y-5 pt-6 pb-16">
              <div className="space-y-6">
                <h2 className="text-xl font-semibold mb-2">Strategy Type</h2>
                <select name="strategyType" value={form.strategyType} onChange={handleChange} className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md">
                  <option>Momentum</option>
                  <option>Mean Reversion</option>
                  <option>Pairs Trading</option>
                  <option>Custom Rule-Based</option>
                </select>

                <h2 className="text-xl font-semibold mt-6 mb-2">Asset Universe</h2>
                <select name="assetUniverse" value={form.assetUniverse} onChange={handleChange} className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md">
                  <option>Single Asset</option>
                  <option>Custom Basket</option>
                  <option>ETF or Sector</option>
                </select>

                <Input label="Asset Symbol / Basket" name="assetSymbol" value={form.assetSymbol} onChange={handleChange} placeholder="e.g., AAPL or NIFTY100" />

                <h2 className="text-xl font-semibold mt-6 mb-2">Timeframe & Dates</h2>
                <select name="timeframe" value={form.timeframe} onChange={handleChange} className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md">
                  <option>1m</option>
                  <option>5m</option>
                  <option>15m</option>
                  <option>Daily</option>
                  <option>Weekly</option>
                </select>

                <div className="flex gap-4 mt-2">
                  <Input label="From" type="date" name="fromDate" value={form.fromDate} onChange={handleChange} />
                  </div>
                  <div> 

                 <Input label="To" type="date" name="toDate" value={form.toDate} onChange={handleChange} />
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2, duration: 0.6 }}
        >
          <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
            <CardContent className="space-y-5 py-6">
              <div className="space-y-6">
                <h2 className="text-xl font-semibold">Strategy Parameters</h2>
                
                <Input label="Entry Condition" name="entryCondition" value={form.entryCondition} onChange={handleChange} placeholder="e.g., SMA50 > SMA200" />
                <Input label="Exit Condition" name="exitCondition" value={form.exitCondition} onChange={handleChange} placeholder="e.g., SMA50 < SMA200" />
                <Slider label="Lookback Period" min={5} max={100} step={1} value={form.lookback} onChange={(v) => setForm({ ...form, lookback: v })} />
                  <div className="grid grid-cols-2 gap-8">
                <Slider label="Initial Capital (₹)" min={10000} max={1000000} step={10000} value={form.capital} onChange={(v) => setForm({ ...form, capital: v })} />
                <Slider label="Leverage (x)" min={1} max={10} step={1} value={form.leverage} onChange={(v) => setForm({ ...form, leverage: v })} />
                <Slider label="Slippage (%)" min={0} max={1} step={0.01} value={form.slippage} onChange={(v) => setForm({ ...form, slippage: v })} />
                <Slider label="Transaction Cost (%)" min={0} max={1} step={0.01} value={form.transactionCost} onChange={(v) => setForm({ ...form, transactionCost: v })} />
                  </div>
                              <div className="space-y-2">
                  <label htmlFor="positionSizing" className="block text-sm font-medium text-gray-300">Position Sizing</label>
                  <select id="positionSizing" name="positionSizing" value={form.positionSizing} onChange={handleChange} className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md">
                    <option>Fixed</option>
                    <option>Percentage of Capital</option>
                    <option>Volatility-Based</option>
                    <option>Kelly Criterion</option>
                  </select>
                </div>

                <div className="space-y-2">
                  <label htmlFor="rebalanceFrequency" className="block text-sm font-medium text-gray-300">Rebalance Frequency</label>
                  <select id="rebalanceFrequency" name="rebalanceFrequency" value={form.rebalanceFrequency} onChange={handleChange} className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md">
                    <option>None</option>
                    <option>Daily</option>
                    <option>Weekly</option>
                    <option>Monthly</option>
                  </select>
                </div>

                
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>

      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid gap-8 py-6">
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2, duration: 0.6 }}
        >
          <Card className="bg-[#1C2433] rounded-2xl shadow-lg">
            <CardContent className="space-y-5 py-6">
              <div className="col-span-1 md:col-span-2 mt-10 space-y-4">
                <h2 className="text-xl font-semibold">Capital & Costs</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  <Slider label="Initial Capital (₹)" min={10000} max={1000000} step={10000} value={form.capital} onChange={(v) => setForm({ ...form, capital: v })} />
                  <Slider label="Leverage (x)" min={1} max={10} step={1} value={form.leverage} onChange={(v) => setForm({ ...form, leverage: v })} />
                  <Slider label="Slippage (%)" min={0} max={1} step={0.01} value={form.slippage} onChange={(v) => setForm({ ...form, slippage: v })} />
                  <Slider label="Transaction Cost (%)" min={0} max={1} step={0.01} value={form.transactionCost} onChange={(v) => setForm({ ...form, transactionCost: v })} />
                </div>

                <div className="space-y-2 mt-6">
                  <label htmlFor="benchmark" className="block text-sm font-medium text-gray-300">Benchmark Index</label>
                  <input type="text" name="benchmark" value={form.benchmark} onChange={handleChange} className="w-full bg-[#2A3548] text-white px-4 py-2 rounded-md" placeholder="e.g., NIFTY100, SPX" />
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>

      <motion.div
        className="text-center mt-12"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.6 }}
      >
        <Button
          onClick={handleRunBacktest}
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg">
          Run Backtest 🚀
        </Button>
      </motion.div>
    </div>
  );
}
