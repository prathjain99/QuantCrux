import React from "react";
import { Link } from "react-router-dom";

export default function ServerError() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#0B1120] text-white p-4">
      <h1 className="text-6xl font-bold text-red-600 mb-4">500</h1>
      <p className="text-xl mb-6">Internal Server Error. Something went wrong on our end.</p>
      <Link to="/" className="text-blue-400 hover:underline">
        Return Home
      </Link>
    </div>
  );
}