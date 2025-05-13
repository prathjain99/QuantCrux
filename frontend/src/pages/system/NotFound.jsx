import React from "react";
import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#0B1120] text-white p-4">
      <h1 className="text-6xl font-bold text-purple-600 mb-4">404</h1>
      <p className="text-xl mb-6">Page Not Found. The page you're looking for doesn't exist.</p>
      <Link to="/" className="text-blue-400 hover:underline">
        Return to Homepage
      </Link>
    </div>
  );
}