

import React from "react";
import { Button } from "@/components/button";
import { Card, CardContent } from "@/components/card";
import {
  BarChart4,
  BrainCircuit,
  PieChart,
  ShieldCheck,
  Sigma,
  Blocks,
} from "lucide-react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { motion } from "framer-motion";
import backgroundImg from "@/assets/chart-glow.png";

const data = [
  { name: "Mon", value: 400 },
  { name: "Tue", value: 600 },
  { name: "Wed", value: 500 },
  { name: "Thu", value: 700 },
  { name: "Fri", value: 800 },
];

const features = [
  {
    title: "Backtesting Engine",
    desc: "Simulate quant strategies over historical data",
    icon: <BarChart4 size={20} />,
  },
  {
    title: "Alpha Signal Discovery",
    desc: "Use ML to detect price signals",
    icon: <BrainCircuit size={20} />,
  },
  {
    title: "Portfolio Optimizer",
    desc: "Construct optimal portfolios",
    icon: <PieChart size={20} />,
  },
  {
    title: "Risk Analytics",
    desc: "Control and assess drawdown risks",
    icon: <ShieldCheck size={20} />,
  },
  {
    title: "Options Pricing",
    desc: "Create rule-based strategies",
    icon: <Sigma size={20} />,
  },
  {
    title: "Strategy Builder",
    desc: "Create role-based or visual strategies",
    icon: <Blocks size={20} />,
  },
];

const fadeUp = {
  hidden: { opacity: 0, y: 20 },
  visible: (i = 1) => ({
    opacity: 1,
    y: 0,
    transition: { delay: i * 0.1, duration: 0.6 },
  }),
};

export default function Home() {
  return (
    <div
      className="bg-[#0B1120] text-[#E2E8F0] min-h-screen px-4 md:px-20 py-10 font-sans relative overflow-hidden"
      style={{
        backgroundImage: `url(${backgroundImg})`,
        backgroundRepeat: "no-repeat",
        backgroundPosition: "center",
        backgroundSize: "cover",
        backgroundAttachment: "fixed",
      }}
    >
      <motion.header
        initial="hidden"
        animate="visible"
        variants={fadeUp}
        className="flex justify-between items-center text-white mb-16"
      >
        <h1 className="text-3xl font-bold">QuantCrux</h1>
        <nav className="space-x-6 text-sm">
          {["Dashboard", "Strategy Lab", "ML Studio", "Docs", "Log In"].map((item, i) => (
            <motion.a
              key={i}
              whileHover={{ scale: 1.1 }}
              href="#"
              className="hover:text-blue-400 transition"
            >
              {item}
            </motion.a>
          ))}
        </nav>
      </motion.header>

      <section className="text-center mb-24">
        <motion.h2
          initial="hidden"
          animate="visible"
          variants={fadeUp}
          className="text-5xl md:text-7xl font-bold mb-4 text-white"
        >
          Build. Backtest. Optimize.
        </motion.h2>
        <motion.p
          initial="hidden"
          animate="visible"
          variants={fadeUp}
          custom={2}
          className="text-lg md:text-xl text-gray-400 mb-6"
        >
          Everything Quant in One Portal.
        </motion.p>
        <motion.p
          initial="hidden"
          animate="visible"
          variants={fadeUp}
          custom={3}
          className="text-md md:text-lg text-gray-500 mb-8"
        >
          AI-augmented tools for strategy design, portfolio management, and risk analytics.
        </motion.p>
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5 }}
        >
          <Button className="bg-primary text-white px-6 py-2 text-md rounded-md shadow-md hover:opacity-90 transition">
            Get Started
          </Button>
        </motion.div>
      </section>

      <section className="mb-20">
        <motion.h3
          initial="hidden"
          animate="visible"
          variants={fadeUp}
          className="text-3xl font-semibold mb-8"
        >
          Core Features
        </motion.h3>
        <div className="grid md:grid-cols-3 gap-6">
          {features.map((f, i) => (
            <motion.div
              key={i}
              custom={i + 1}
              initial="hidden"
              animate="visible"
              variants={fadeUp}
              whileHover={{ scale: 1.03 }}
            >
              <Card className="bg-[#1C2433] hover:bg-[#243044] transition-colors p-4">
                <CardContent className="space-y-4">
                  <div className="flex items-center space-x-2">
                    {f.icon}
                    <h4 className="text-lg font-medium text-white">{f.title}</h4>
                  </div>
                  <p className="text-sm text-gray-400">{f.desc}</p>
                  <Button variant="secondary" className="w-full bg-[#3B82F6]/10 text-blue-400 hover:bg-[#3B82F6]/20">
                    Launch
                  </Button>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>
      </section>

      <section>
        <motion.h3
          initial="hidden"
          animate="visible"
          variants={fadeUp}
          className="text-3xl font-semibold mb-8"
        >
          Live Data/Market Dashboard
        </motion.h3>
        <div className="grid md:grid-cols-3 gap-6">
          <motion.div whileHover={{ scale: 1.02 }}>
            <Card className="bg-[#1C2433] p-4">
              <CardContent>
                <h4 className="text-lg font-medium mb-2">Top-performing assets</h4>
                <ul className="text-sm space-y-1">
                  <li>
                    Tesla Inc. <span className="text-green-500">+3.15%</span>
                  </li>
                  <li>
                    Alphabet Inc. <span className="text-green-500">+2.48%</span>
                  </li>
                  <li>
                    Amazon.com, Inc. <span className="text-green-500">+1.52%</span>
                  </li>
                </ul>
              </CardContent>
            </Card>
          </motion.div>

          <motion.div whileHover={{ scale: 1.02 }}>
            <Card className="bg-[#1C2433] p-4">
              <CardContent>
                <h4 className="text-lg font-medium mb-2">Real-time chart</h4>
                <ResponsiveContainer width="100%" height={100}>
                  <LineChart data={data}>
                    <Line type="monotone" dataKey="value" stroke="#3B82F6" strokeWidth={2} dot={false} />
                    <XAxis dataKey="name" hide />
                    <YAxis hide />
                    <Tooltip />
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </motion.div>

          <motion.div whileHover={{ scale: 1.02 }}>
            <Card className="bg-[#1C2433] p-4 text-center">
              <CardContent>
                <h4 className="text-lg font-medium mb-2">Sentiment Index</h4>
                <p className="text-2xl">Neutral</p>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </section>

      <footer className="mt-24 text-center text-sm text-gray-500">
        <div className="space-x-4">
          <a href="#">Company</a>
          <a href="#">GitHub</a>
          <a href="#">Terms</a>
          <a href="#">Privacy</a>
        </div>
        <p className="mt-2">&copy; 2025 QuantCrux. All rights reserved.</p>
      </footer>
    </div>
  );
}