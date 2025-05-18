
import React, { useState, useEffect, useMemo } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";
import { motion } from "framer-motion";
import { Button } from "@/components/Button";
import Papa from "papaparse";

import { useNavigate } from "react-router-dom";
import Header from "../../components/header";
import ExportButtons from "../../components/exportButton";

export default function BacktestResults() {
  const [metrics, setMetrics] = useState(null);
  const [equityCurve, setEquityCurve] = useState([]);
  const [drawdowns, setDrawdowns] = useState([]);
  const [tradeLog, setTradeLog] = useState([]);
  const [sortAsc, setSortAsc] = useState(true);
  const navigate = useNavigate();

  // Simulated benchmark data for comparison chart
  const [benchmarkCurve, setBenchmarkCurve] = useState([]);

  useEffect(() => {
    // Simulated fetch
    const fetchData = async () => {
      setMetrics({
        CAGR: "17.5%",
        Sharpe: "1.42",
        Sortino: "2.1",
        MaxDrawdown: "-8.2%",
        WinRate: "61%",
        AvgTradeDuration: "3.2 days",
        TotalTrades: 45,
        ProfitFactor: "1.75",
      });

      const equity = [
        { date: "2023-01", equity: 100000 },
        { date: "2023-02", equity: 103000 },
        { date: "2023-03", equity: 105500 },
        { date: "2023-04", equity: 101200 },
        { date: "2023-05", equity: 110400 },
      ];
      setEquityCurve(equity);

      // Calculate drawdowns for equity curve
      let peak = -Infinity;
      const dd = equity.map(({ date, equity }) => {
        if (equity > peak) peak = equity;
        const drawdown = ((equity - peak) / peak) * 100;
        return { date, drawdown };
      });
      setDrawdowns(dd);

      setTradeLog([
        { date: "2023-01-15", asset: "AAPL", entry: 150, exit: 157, pnl: 700 },
        { date: "2023-02-10", asset: "MSFT", entry: 300, exit: 290, pnl: -500 },
        { date: "2023-03-22", asset: "GOOG", entry: 1300, exit: 1350, pnl: 1000 },
        { date: "2023-04-02", asset: "TSLA", entry: 800, exit: 825, pnl: 1250 },
        { date: "2023-04-15", asset: "AMZN", entry: 3500, exit: 3400, pnl: -1500 },
      ]);

      setBenchmarkCurve([
        { date: "2023-01", value: 100000 },
        { date: "2023-02", value: 102000 },
        { date: "2023-03", value: 104000 },
        { date: "2023-04", value: 103500 },
        { date: "2023-05", value: 108000 },
      ]);
    };

    fetchData();
  }, []);

  // Derived trade stats
  const tradeStats = useMemo(() => {
    if (!tradeLog.length) return {};
    const profits = tradeLog.map((t) => t.pnl);
    const avgProfit =
      profits.reduce((sum, val) => sum + val, 0) / tradeLog.length;
    const maxWin = Math.max(...profits);
    const maxLoss = Math.min(...profits);

    // Calculate consecutive wins/losses streak
    let maxWinStreak = 0,
      maxLossStreak = 0,
      currentWinStreak = 0,
      currentLossStreak = 0;
    tradeLog.forEach(({ pnl }) => {
      if (pnl > 0) {
        currentWinStreak++;
        maxWinStreak = Math.max(maxWinStreak, currentWinStreak);
        currentLossStreak = 0;
      } else {
        currentLossStreak++;
        maxLossStreak = Math.max(maxLossStreak, currentLossStreak);
        currentWinStreak = 0;
      }
    });

    return {
      avgProfit,
      maxWin,
      maxLoss,
      maxWinStreak,
      maxLossStreak,
    };
  }, [tradeLog]);

  const handleCSVExport = () => {
    const csv = Papa.unparse(tradeLog);
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", "trade_log.csv");
    link.click();
  };

  const handleRunBacktestResult = () => {
    navigate("/backtest");
  };

  // Sort trade log by pnl
  const sortedTradeLog = useMemo(() => {
    return [...tradeLog].sort((a, b) =>
      sortAsc ? a.pnl - b.pnl : b.pnl - a.pnl
    );
  }, [tradeLog, sortAsc]);

  return (
    <div className="bg-[#0B1120] min-h-screen px-8 py-12 text-white">
      <Header showHome />
      <motion.h1
        className="text-4xl font-bold mb-10 text-center"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        📈 Backtest Results
      </motion.h1>

      {/* 🔹 Metrics Summary */}
      <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-8 gap-4 text-sm mb-10">
        {metrics &&
          Object.entries(metrics).map(([key, value]) => (
            <div
              key={key}
              className="bg-[#1C2433] p-4 rounded-lg text-center border border-gray-700"
            >
              <div className="text-gray-400">{key}</div>
              <div className="text-lg font-semibold text-blue-400">{value}</div>
            </div>
          ))}
      </div>

     

      {/* 🔹 Trade Stats */}
      <div className="bg-[#1C2433] p-6 rounded-xl border border-gray-700 mb-10 grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-4 text-sm">
        <div className="text-center border border-gray-700 p-3 rounded">
          <div className="text-gray-400">Avg Profit (₹)</div>
          <div className="text-lg font-semibold text-green-400">
            {tradeStats.avgProfit?.toFixed(2) ?? "-"}
          </div>
        </div>
        <div className="text-center border border-gray-700 p-3 rounded">
          <div className="text-gray-400">Largest Win (₹)</div>
          <div className="text-lg font-semibold text-green-400">
            {tradeStats.maxWin?.toFixed(2) ?? "-"}
          </div>
        </div>
        <div className="text-center border border-gray-700 p-3 rounded">
          <div className="text-gray-400">Largest Loss (₹)</div>
          <div className="text-lg font-semibold text-red-400">
            {tradeStats.maxLoss?.toFixed(2) ?? "-"}
          </div>
        </div>
        <div className="text-center border border-gray-700 p-3 rounded">
          <div className="text-gray-400">Max Win Streak</div>
          <div className="text-lg font-semibold text-green-400">
            {tradeStats.maxWinStreak ?? "-"}
          </div>
        </div>
        <div className="text-center border border-gray-700 p-3 rounded">
          <div className="text-gray-400">Max Loss Streak</div>
          <div className="text-lg font-semibold text-red-400">
            {tradeStats.maxLossStreak ?? "-"}
          </div>
        </div>
      </div>

      {/* 🔹 Trade Logs */}
      <div className="bg-[#1C2433] p-6 rounded-xl border border-gray-700 mb-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold">Trade Log</h2>
          <div className="flex gap-2 items-center">
            <Button
              className="bg-blue-600 text-white px-4 py-1 rounded-md text-sm"
              onClick={handleCSVExport}
            >
              Export CSV
            </Button>
            <Button
              className="bg-gray-600 text-white px-4 py-1 rounded-md text-sm"
              onClick={() => setSortAsc(!sortAsc)}
            >
              Sort PnL {sortAsc ? "▲" : "▼"}
            </Button>
          </div>
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
              {sortedTradeLog.map((trade, index) => (
                <tr
                  key={index}
                  className="border-b border-gray-800 hover:bg-gray-800"
                >
                  <td className="px-3 py-2">{trade.date}</td>
                  <td className="px-3 py-2">{trade.asset}</td>
                  <td className="px-3 py-2">{trade.entry}</td>
                  <td className="px-3 py-2">{trade.exit}</td>
                  <td
                    className={`px-3 py-2 ${
                      trade.pnl >= 0 ? "text-green-400" : "text-red-400"
                    }`}
                  >
                    {trade.pnl}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="bg-[#1C2433] p-6 rounded-xl border border-gray-700 mb-6 text-center">
  <h2 className="text-lg font-semibold mb-4">📤 Export Your Data</h2>
  <ExportButtons onClick={handleCSVExport} />
</div>

      <motion.div
        className="text-center mt-12"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.6 }}
      >
        <Button
          onClick={handleRunBacktestResult}
          className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 text-lg rounded-xl hover:scale-105 transition-transform shadow-lg"
        >
          Re-Run Backtest 🚀
        </Button>
      </motion.div>
    </div>
  );
}
