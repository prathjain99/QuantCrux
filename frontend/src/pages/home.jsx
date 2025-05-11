import React from "react";
import { Button } from "@/components/button";
import { Card, CardContent } from "@/components/card";
import { BarChart4, BrainCircuit, PieChart, ShieldCheck, Sigma, Blocks } from "lucide-react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";

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
    desc: "Control & assess drawdown risks",
    icon: <ShieldCheck size={20} />,
  },
  {
    title: "Options Pricing",
    desc: "Create rule-based strategies",
    icon: <Sigma size={20} />,
  },
  {
    title: "Strategy Builder",
    desc: "Visualize and design strategies",
    icon: <Blocks size={20} />,
  },
];

export default function Home() {
  return (
    <div className="bg-background text-text min-h-screen px-4 md:px-20 py-10 font-sans">
      {/* Header */}
      <header className="flex justify-between items-center text-white mb-10">
        <h1 className="text-2xl font-bold">QuantCrux</h1>
        <nav className="space-x-6">
          <a href="#">Dashboard</a>
          <a href="#">Strategy Lab</a>
          <a href="#">ML Studio</a>
          <a href="#">Docs</a>
          <a href="#">Log In</a>
        </nav>
      </header>

      {/* Hero Section */}
      <section className="text-center mb-20">
        <h2 className="text-4xl md:text-6xl font-bold mb-4">Build. Backtest. Optimize.</h2>
        <p className="text-lg md:text-xl mb-6">Everything Quant in One Portal.</p>
        <p className="text-md md:text-lg text-gray-400 mb-6">AI-augmented tools for strategy design, portfolio management, and risk analytics.</p>
        <Button className="bg-primary text-white px-6 py-2 rounded-md hover:opacity-90">Get Started</Button>
      </section>

      {/* Core Features */}
      <section className="mb-16">
        <h3 className="text-2xl font-semibold mb-6">Core Features</h3>
        <div className="grid md:grid-cols-3 gap-6">
          {features.map((f, i) => (
            <Card key={i} className="bg-card p-4 text-left">
              <CardContent className="space-y-4">
                <div className="flex items-center space-x-2">
                  {f.icon}
                  <h4 className="text-lg font-medium">{f.title}</h4>
                </div>
                <p className="text-sm text-gray-400">{f.desc}</p>
                <Button variant="secondary">Launch</Button>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Live Dashboard */}
      <section>
        <h3 className="text-2xl font-semibold mb-6">Live Data/Market Dashboard</h3>
        <div className="grid md:grid-cols-3 gap-6">
          <Card className="bg-card p-4">
            <CardContent>
              <h4 className="text-lg font-medium mb-2">Top-performing assets</h4>
              <ul className="text-sm space-y-1">
                <li>Tesla Inc. <span className="text-green-500">+3.15%</span></li>
                <li>Alphabet Inc. <span className="text-green-500">+2.48%</span></li>
                <li>Amazon.com, Inc. <span className="text-green-500">+1.52%</span></li>
              </ul>
            </CardContent>
          </Card>

          <Card className="bg-card p-4">
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

          <Card className="bg-card p-4 text-center">
            <CardContent>
              <h4 className="text-lg font-medium mb-2">Sentiment Index</h4>
              <p className="text-2xl">Neutral</p>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Footer */}
      <footer className="mt-20 text-center text-sm text-gray-400">
        <div className="space-x-4">
          <a href="#">Company</a>
          <a href="#">Github</a>
          <a href="#">Terms</a>
          <a href="#">Privacy</a>
        </div>
        <p className="mt-2">&copy; 2025 QuantCrux. All rights reserved.</p>
      </footer>
    </div>
  );
}
