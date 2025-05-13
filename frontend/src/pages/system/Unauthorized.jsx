import React from "react";
import { Link } from "react-router-dom";

export default function Unauthorized() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#0B1120] text-white p-4">
      <h1 className="text-6xl font-bold text-yellow-500 mb-4">401</h1>
      <p className="text-xl mb-6">Unauthorized Access. You do not have permission to view this page.</p>
      <Link to="/log-in" className="text-blue-400 hover:underline">
        Go to Login
      </Link>
    </div>
  );
}