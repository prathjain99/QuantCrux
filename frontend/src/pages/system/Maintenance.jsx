
import React from "react";

export default function Maintenance() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#0B1120] text-white p-4">
      <h1 className="text-6xl font-bold text-gray-500 mb-4">Maintenance</h1>
      <p className="text-xl text-center max-w-lg">
        We're currently performing scheduled maintenance. Please check back later.
      </p>
    </div>
  );
}