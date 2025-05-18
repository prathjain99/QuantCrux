import React, { useState, useEffect } from "react";
import axios from "axios";
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
import { Link, useNavigate } from "react-router-dom"; // Import Link and useNavigate
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
    title: "Regime Detection",
    desc: "Identify market regimes for smarter trading decisions.",
    icon: <Blocks size={20} />,
    link: "/regime-detection", // Link to the strategy builder page
  },

  {
    title: "Alpha Signal Discovery",
    desc: "Use ML to detect price signals",
    icon: <BrainCircuit size={20} />,
    link: "/alpha-signals", // Link to the alpha signal discovery page
  },

  {
    title: "Backtesting Engine",
    desc: "Simulate quant strategies over historical data",
    icon: <BarChart4 size={20} />,
    link: "/backtest", // Link to the backtesting page
  },

  {
    title: "Portfolio Optimizer",
    desc: "Construct optimal portfolios",
    icon: <PieChart size={20} />,
    link: "/portfolio", // Link to the portfolio optimizer page
  },
  {
    title: "Risk Analytics",
    desc: "Control and assess drawdown risks",
    icon: <ShieldCheck size={20} />,
    link: "/risk-analytics", // Link to the risk analytics page
  },

  {
    title: "Options Pricing",
    desc: "Create rule-based strategies",
    icon: <Sigma size={20} />,
    link: "/options-pricing", // Link to the options pricing page
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
  console.log("Home component render");
  const TWELVE_API_KEY = import.meta.env.VITE_TWELVE_API_KEY;
  const ALPHA_API_KEY = import.meta.env.VITE_ALPHA_API_KEY;

  const [topStocks, setTopStocks] = useState([]);
  const [loadingStocks, setLoadingStocks] = useState(true);

  useEffect(() => {
    console.log("Component mounted");

    const symbols = ["AAPL", "GOOGL", "TSLA", "TCS"]; // Use correct symbols for Alpha Vantage

    const fetchStocks = async () => {
      try {
        const symbols = ["AAPL", "GOOGL", "TSLA", "RELIANCE", "TCS"];

        const promises = symbols.map((symbol) =>
          axios.get(`https://www.alphavantage.co/query`, {
            params: {
              function: "GLOBAL_QUOTE",
              symbol,
              apikey: ALPHA_API_KEY,
            },
          })
        );

        const results = await Promise.all(promises);

        const formatted = results
          .map((res) => {
            console.log("Alpha Vantage response:", res.data);
            const quote = res.data["Global Quote"];
            if (!quote || !quote["01. symbol"]) return null;
            return {
              name: quote["01. symbol"],
              change: parseFloat(quote["10. change percent"].replace("%", "")),
            };
          })
          .filter((stock) => stock !== null)
          .sort((a, b) => b.change - a.change)
          .slice(0, 5);

        setTopStocks(formatted);
      } catch (err) {
        console.error("Error fetching stock data from Alpha Vantage:", err);
      } finally {
        setLoadingStocks(false);
      }
    };

    fetchStocks();
  }, []);

  const navigate = useNavigate(); // Initialize useNavigate

  // Handle navigation for the "Get Started" button (Optional)
  const handleGetStarted = () => {
    navigate("/register"); // Replace with the actual page for getting started
  };

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
          {[
            { name: "Dashboard", path: "/dashboard" },
            { name: "Strategy Lab", path: "/strategy-lab" },
            { name: "ML Studio", path: "/ml-studio" },
            { name: "Docs", path: "/docs" },
            { name: "Log In", path: "/log-in" },
          ].map((item, i) => (
            <motion.div
              key={i}
              whileHover={{ scale: 1.1 }}
              className="inline-block"
            >
              <Link to={item.path} className="hover:text-blue-400 transition">
                {item.name}
              </Link>
            </motion.div>
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
          AI-augmented tools for strategy design, portfolio management, and risk
          analytics.
        </motion.p>
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5 }}
        >
          <Button
            onClick={handleGetStarted}
            className="bg-primary text-white px-6 py-2 text-md rounded-md shadow-md hover:opacity-90 transition"
          >
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
          {/* Core Features */}
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
              <Link to={f.link}>
                {" "}
                {/* Add navigation for each feature */}
                <Card className="bg-[#1C2433] hover:bg-[#243044] transition-colors p-4">
                  <CardContent className="space-y-4">
                    <div className="flex items-center space-x-2">
                      {f.icon}
                      <h4 className="text-lg font-medium text-white">
                        {f.title}
                      </h4>
                    </div>
                    <p className="text-sm text-gray-400">{f.desc}</p>
                    <Button
                      variant="secondary"
                      className="w-full bg-[#3B82F6]/10 text-blue-400 hover:bg-[#3B82F6]/20"
                    >
                      Launch
                    </Button>
                  </CardContent>
                </Card>
              </Link>
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
                <h4 className="text-lg font-medium mb-2">
                  Top-performing assets
                </h4>
                {loadingStocks ? (
                  <p className="text-sm text-gray-500">Loading...</p>
                ) : (
                  <ul className="text-sm space-y-1">
                    {topStocks.map((stock, idx) => (
                      <li key={idx}>
                        {stock.name}{" "}
                        <span
                          className={`ml-2 ${
                            stock.change >= 0
                              ? "text-green-500"
                              : "text-red-500"
                          }`}
                        >
                          {stock.change >= 0 ? "+" : ""}
                          {stock.change.toFixed(2)}%
                        </span>
                      </li>
                    ))}
                  </ul>
                )}
              </CardContent>
            </Card>
          </motion.div>

          <motion.div whileHover={{ scale: 1.02 }}>
            <Card className="bg-[#1C2433] p-4">
              <CardContent>
                <h4 className="text-lg font-medium mb-2">Real-time chart</h4>
                <ResponsiveContainer width="100%" height={100}>
                  <LineChart data={data}>
                    <Line
                      type="monotone"
                      dataKey="value"
                      stroke="#3B82F6"
                      strokeWidth={2}
                      dot={false}
                    />
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
