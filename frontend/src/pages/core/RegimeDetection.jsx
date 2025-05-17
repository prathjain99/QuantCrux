
// import React, { useState } from "react";
// import { useNavigate } from "react-router-dom";
// import { motion } from "framer-motion";
// import { Button } from "@/components/button";
// import Input from "@/components/input";
// import { Card, CardContent } from "@/components/card";
// import Label from "@/components/label";
// import Header from "@/components/header";

// export default function RegimeDetection() {
//   const navigate = useNavigate();
//   const [form, setForm] = useState({
//     assetTickers: "NIFTY, RELIANCE",
//     frequency: "daily",
//     lookback: 252,
//     method: "non-ml",
//     rollingWindow: 30,
//     numRegimes: 3,
//     useOptimizerOutput: false,
//   });

//   const handleChange = (e) => {
//     const { name, value, type, checked } = e.target;
//     setForm((prev) => ({
//       ...prev,
//       [name]: type === "checkbox" ? checked : value,
//     }));
//   };

//   const handleSubmit = () => {
//     navigate("/regime-detection-results", { state: { formData: form } });
//   };

//   return (
//     <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-14">
//       <Header showHome />

//       {/* Page Title */}
//       <motion.div
//         initial={{ opacity: 0, y: 30 }}
//         animate={{ opacity: 1, y: 0 }}
//         transition={{ duration: 0.6 }}
//         className="mb-10 text-center"
//       >
//         <h1 className="text-4xl font-bold mb-2">Market Regime Detection</h1>
//         <p className="text-gray-400 max-w-2xl mx-auto">
//           Select method and input parameters to detect market regimes using statistical or ML-based approaches.
//         </p>
//       </motion.div>

//       {/* Form Card */}
//       <Card className="bg-[#1C2433] rounded-2xl">
//         <CardContent className="p-6">
//           <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          

//             {/* Asset Tickers Input */}
//             {!form.useOptimizerOutput && (
//               <div className="col-span-2">
//                 <Label htmlFor="assetTickers" className="text-white">Asset Tickers (comma-separated)</Label>
//                 <Input
//                   name="assetTickers"
//                   value={form.assetTickers}
//                   onChange={handleChange}
//                   placeholder="e.g. NIFTY, RELIANCE, INFY"
//                   className="mt-2"
//                 />
//               </div>
//             )}

//             {/* Frequency */}
//             <div>
//               <Label htmlFor="frequency" className="text-white">Frequency</Label>
//               <select
//                 name="frequency"
//                 value={form.frequency}
//                 onChange={handleChange}
//                 className="w-full mt-2 p-2 bg-[#2A3548] text-white rounded-lg"
//               >
//                 <option value="daily">Daily</option>
//                 <option value="weekly">Weekly</option>
//                 <option value="monthly">Monthly</option>
//               </select>
//             </div>

//             {/* Lookback */}
//             <div>
//               <Label htmlFor="lookback" className="text-white">Lookback Period</Label>
//               <Input
//                 type="number"
//                 name="lookback"
//                 value={form.lookback}
//                 onChange={handleChange}
//                 placeholder="e.g. 252"
//                 className="mt-2"
//               />
//             </div>

//             {/* Detection Method */}
//             <div>
//               <Label htmlFor="method" className="text-white">Detection Method</Label>
//               <select
//                 name="method"
//                 value={form.method}
//                 onChange={handleChange}
//                 className="w-full mt-2 p-2 bg-[#2A3548] text-white rounded-lg"
//               >
//                 <option value="non-ml">Non-ML (Volatility, Breaks)</option>
//                 <option value="kmeans">K-Means</option>
//                 <option value="gmm">GMM</option>
//                 <option value="hmm">HMM</option>
//               </select>
//             </div>

//             {/* Number of Regimes */}
//             {form.method !== "non-ml" && (
//               <div>
//                 <Label htmlFor="numRegimes" className="text-white">Number of Regimes</Label>
//                 <Input
//                   type="number"
//                   name="numRegimes"
//                   value={form.numRegimes}
//                   onChange={handleChange}
//                   className="mt-2"
//                 />
//               </div>
//             )}

//             {/* Rolling Window */}
//             <div>
//               <Label htmlFor="rollingWindow" className="text-white">Rolling Window (Optional)</Label>
//               <Input
//                 type="number"
//                 name="rollingWindow"
//                 value={form.rollingWindow}
//                 onChange={handleChange}
//                 placeholder="e.g. 30"
//                 className="mt-2"
//               />
//             </div>
//           </div>

//           {/* Submit Button */}
//           <div className="text-center mt-10">
//             <Button
//               onClick={handleSubmit}
//               className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform"
//             >
//               Detect Market Regimes 📊
//             </Button>
//           </div>
//         </CardContent>
//       </Card>
//     </div>
//   );
// }


import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Button } from "@/components/button";
import Input from "@/components/input";
import { Card, CardContent } from "@/components/card";
import Label from "@/components/label";
import Header from "@/components/header";

export default function RegimeDetection() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    assetTickers: "NIFTY, RELIANCE",
    frequency: "daily",
    lookback: 252,
    method: "kmeans",
    rollingWindow: 30,
    numRegimes: 3,
    useOptimizerOutput: false,
    featureType: "returns",
    regimeLabeling: "auto",
    normalize: false,
    applyPCA: false,
    zscore: false,
    startDate: "",
    endDate: ""
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = () => {
    navigate("/regime-detection-results", { state: { formData: form } });
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
        <h1 className="text-4xl font-bold mb-2">Market Regime Detection</h1>
        <p className="text-gray-400 max-w-2xl mx-auto">
          Select method and input parameters to detect market regimes using statistical or ML-based approaches.
        </p>
      </motion.div>

      <Card className="bg-[#1C2433] rounded-2xl">
        <CardContent className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {!form.useOptimizerOutput && (
              <div className="col-span-2">
                <Label htmlFor="assetTickers" className="text-white">Asset Tickers (comma-separated)</Label>
                <Input
                  name="assetTickers"
                  value={form.assetTickers}
                  onChange={handleChange}
                  placeholder="e.g. NIFTY, RELIANCE, INFY"
                  className="mt-2"
                />
              </div>
            )}

            <div>
              <Label htmlFor="frequency" className="text-white">Frequency</Label>
              <select
                name="frequency"
                value={form.frequency}
                onChange={handleChange}
                className="w-full mt-2 p-2 bg-[#2A3548] text-white rounded-lg"
              >
                <option value="daily">Daily</option>
                <option value="weekly">Weekly</option>
                <option value="monthly">Monthly</option>
              </select>
            </div>

            <div>
              <Label htmlFor="lookback" className="text-white">Lookback Period</Label>
              <Input
                type="number"
                name="lookback"
                value={form.lookback}
                onChange={handleChange}
                placeholder="e.g. 252"
                className="mt-2"
              />
            </div>

            <div>
              <Label htmlFor="method" className="text-white">Detection Method</Label>
              <select
                name="method"
                value={form.method}
                onChange={handleChange}
                className="w-full mt-2 p-2 bg-[#2A3548] text-white rounded-lg"
              >
                <option value="kmeans">K-Means</option>
                <option value="gmm">GMM</option>
                <option value="hmm">HMM</option>
                <option value="non-ml">Non-ML (Volatility, Breaks)</option>
              </select>
            </div>

            {form.method !== "non-ml" && (
              <div>
                <Label htmlFor="numRegimes" className="text-white">Number of Regimes</Label>
                <Input
                  type="number"
                  name="numRegimes"
                  value={form.numRegimes}
                  onChange={handleChange}
                  className="mt-2"
                />
              </div>
            )}

            <div>
              <Label htmlFor="rollingWindow" className="text-white">Rolling Window (Optional)</Label>
              <Input
                type="number"
                name="rollingWindow"
                value={form.rollingWindow}
                onChange={handleChange}
                placeholder="e.g. 30"
                className="mt-2"
              />
            </div>

            <div>
              <Label htmlFor="featureType" className="text-white">Feature for Regime Detection</Label>
              <select
                name="featureType"
                value={form.featureType}
                onChange={handleChange}
                className="w-full mt-2 p-2 bg-[#2A3548] text-white rounded-lg"
              >
                <option value="returns">Returns</option>
                <option value="volatility">Volatility</option>
                <option value="correlation">Correlation</option>
                <option value="momentum">Momentum</option>
              </select>
            </div>

            <div>
              <Label htmlFor="regimeLabeling" className="text-white">Pre-Processing Options</Label>
              <select
                name="regimeLabeling"
                value={form.regimeLabeling}
                onChange={handleChange}
                className="w-full mt-2 p-2 bg-[#2A3548] text-white rounded-lg"
              >
                <option value="auto">Auto-label (volatility-based)</option>
                <option value="manual">Manual</option>
                <option value="raw">Cluster ID Only</option>
              </select>
            </div>
            
            <div>
              <Label htmlFor="regimeLabeling" className="text-white">Regime Labeling</Label>
            
            <Card>
              <CardContent>
            <div className="col-span-2 grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="flex items-center space-x-2">
                  <input type="checkbox" name="normalize" checked={form.normalize} onChange={handleChange} />
                  <span>Normalize Returns</span>
                </label>
              </div>
              <div>
                <label className="flex items-center space-x-2">
                  <input type="checkbox" name="applyPCA" checked={form.applyPCA} onChange={handleChange} />
                  <span>Apply PCA</span>
                </label>
              </div>
              <div>
                <label className="flex items-center space-x-2">
                  <input type="checkbox" name="zscore" checked={form.zscore} onChange={handleChange} />
                  <span>Z-Score Scaling</span>
                </label>
              </div>
            </div>
            </CardContent>
            </Card>
            </div>
            
            

            <div>
              <Label htmlFor="startDate" className="text-white">Start Date (optional)</Label>
              <Input
                type="date"
                name="startDate"
                value={form.startDate}
                onChange={handleChange}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="endDate" className="text-white">End Date (optional)</Label>
              <Input
                type="date"
                name="endDate"
                value={form.endDate}
                onChange={handleChange}
                className="mt-2"
              />
            </div>
          </div>

          <div className="text-center mt-10">
            <Button
              onClick={handleSubmit}
              className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform"
            >
              Detect Market Regimes 📊
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

