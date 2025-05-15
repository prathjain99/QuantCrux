import React from "react";
import { Link, useLocation } from "react-router-dom";
import { motion } from "framer-motion";

const fadeUp = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5 } },
};

export default function Header({ showHome = false }) {
  const location = useLocation();

  const navItems = [
    ...(showHome ? [{ name: "Home", path: "/" }] : []),
    { name: "Dashboard", path: "/dashboard" },
    { name: "Strategy Lab", path: "/strategy-lab" },
    { name: "ML Studio", path: "/ml-studio" },
    { name: "Docs", path: "/docs" },
    { name: "Log In", path: "/log-in" },
  ];

  return (
    <motion.header
      initial="hidden"
      animate="visible"
      variants={fadeUp}
      className="flex justify-between items-center text-white mb-16"
    >
      <h1 className="text-3xl font-bold">QuantCrux</h1>
      <nav className="space-x-6 text-sm">
        {navItems.map((item, i) => (
          <motion.div key={i} whileHover={{ scale: 1.1 }} className="inline-block">
            <Link
              to={item.path}
              className={`hover:text-blue-400 transition ${
                location.pathname === item.path ? "text-blue-400 font-semibold" : ""
              }`}
            >
              {item.name}
            </Link>
          </motion.div>
        ))}
      </nav>
    </motion.header>
  );
}
