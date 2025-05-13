import React from "react";
import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import Input from "@/components/Input";
import { Button } from "@/components/button";

const fadeUp = {
  hidden: { opacity: 0, y: 20 },
  visible: (i = 1) => ({
    opacity: 1,
    y: 0,
    transition: { delay: i * 0.1, duration: 0.6 },
  }),
};

export default function Register() {
  return (
    <div className="bg-[#0B1120] text-[#E2E8F0] min-h-screen flex flex-col items-center justify-center px-4 py-10">
      <motion.div
        initial="hidden"
        animate="visible"
        variants={fadeUp}
        className="w-full max-w-md bg-[#1C2433] rounded-2xl p-8 shadow-lg"
      >
        <h2 className="text-3xl font-bold text-center mb-6 text-white">
          Create Your Account
        </h2>

        <form className="space-y-4">
          <div>
            <label className="text-sm text-gray-300">Full Name</label>
            <Input type="text" placeholder="Your Name" className="mt-1 bg-[#0F172A] border-none" />
          </div>
          <div>
            <label className="text-sm text-gray-300">Email</label>
            <Input type="email" placeholder="you@example.com" className="mt-1 bg-[#0F172A] border-none" />
          </div>
          <div>
            <label className="text-sm text-gray-300">Password</label>
            <Input type="password" placeholder="••••••••" className="mt-1 bg-[#0F172A] border-none" />
          </div>

          <Button type="submit" className="w-full mt-4 bg-blue-600 hover:bg-blue-700">
            Register
          </Button>
        </form>

        <p className="text-sm text-center text-gray-400 mt-6">
          Already have an account?{' '}
          <Link to="/log-in" className="text-blue-400 hover:underline">
            Log In
          </Link>
        </p>
      </motion.div>
    </div>
  );
}
