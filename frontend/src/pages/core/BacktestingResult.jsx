import React, { useState, useEffect } from "react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { motion } from "framer-motion";
import { Button } from "@/components/Button";
import Papa from "papaparse";

export default function BacktestResults() {
  const [metrics, setMetrics] = useState(null);
  const [equityCurve, setEquityCurve] = useState([]);
  const [tradeLog, setTradeLog] = useState([]);

  useEffect(() => {
    // 🔄 Replace with real API data
    const fetchData = async () => {
      // Simulated fetch
      setMetrics({
        CAGR: "17.5%",
        Sharpe: "1.42",
        Sortino: "2.1",
        MaxDrawdown: "-8.2%",
        WinRate: "61%",
        AvgTradeDuration: "3.2 days",
      });

      setEquityCurve([
        { date: "2023-01", equity: 100000 },
        { date: "2023-02", equity: 103000 },
        { date: "2023-03", equity: 105500 },
        { date: "2023-04", equity: 101200 },
        { date: "2023-05", equity: 110400 },
      ]);

      setTradeLog([
        { date: "2023-01-15", asset: "AAPL", entry: 150, exit: 157, pnl: 700 },
        { date: "2023-02-10", asset: "MSFT", entry: 300, exit: 290, pnl: -500 },
        { date: "2023-03-22", asset: "GOOG", entry: 1300, exit: 1350, pnl: 1000 },
      ]);
    };

    fetchData();
  }, []);

  const handleCSVExport = () => {
    const csv = Papa.unparse(tradeLog);
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", "trade_log.csv");
    link.click();
  };

  return (
    <div className="bg-[#0B1120] min-h-screen px-8 py-12 text-white">
      <motion.h1
        className="text-4xl font-bold mb-10 text-center"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        📈 Backtest Results
      </motion.h1>

      {/* 🔹 Metrics Summary */}
      <div className="grid grid-cols-1 sm:grid-cols-3 md:grid-cols-6 gap-4 text-sm mb-10">
        {metrics &&
          Object.entries(metrics).map(([key, value]) => (
            <div key={key} className="bg-[#1C2433] p-4 rounded-lg text-center border border-gray-700">
              <div className="text-gray-400">{key}</div>
              <div className="text-lg font-semibold text-blue-400">{value}</div>
            </div>
          ))}
      </div>

      {/* 🔹 Equity Curve */}
      <div className="bg-[#1C2433] p-6 rounded-xl border border-gray-700 mb-10">
        <h2 className="text-lg font-semibold mb-4">Equity Curve</h2>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={equityCurve}>
            <CartesianGrid strokeDasharray="3 3" stroke="#2d3748" />
            <XAxis dataKey="date" stroke="#cbd5e1" />
            <YAxis stroke="#cbd5e1" />
            <Tooltip />
            <Line type="monotone" dataKey="equity" stroke="#38bdf8" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* 🔹 Trade Logs */}
      <div className="bg-[#1C2433] p-6 rounded-xl border border-gray-700 mb-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold">Trade Log</h2>
          <Button className="bg-blue-600 text-white px-4 py-1 rounded-md text-sm" onClick={handleCSVExport}>
            Export CSV
          </Button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="text-gray-400 border-b border-gray-700">
              <tr>
                <th className="px-3 py-2">Date</th>
                <th className="px-3 py-2">Asset</th>
                <th className="px-3 py-2">Entry</th>
                <th className="px-3 py-2">Exit</th>
                <th className="px-3 py-2">PnL (₹)</th>
              </tr>
            </thead>
            <tbody>
              {tradeLog.map((trade, index) => (
                <tr key={index} className="border-b border-gray-800 hover:bg-gray-800">
                  <td className="px-3 py-2">{trade.date}</td>
                  <td className="px-3 py-2">{trade.asset}</td>
                  <td className="px-3 py-2">{trade.entry}</td>
                  <td className="px-3 py-2">{trade.exit}</td>
                  <td className={`px-3 py-2 ${trade.pnl >= 0 ? "text-green-400" : "text-red-400"}`}>{trade.pnl}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
