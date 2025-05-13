import React from "react";

export default function Input({ label, type = "text", name, value, onChange, placeholder }) {
  return (
    <div className="w-full space-y-1">
      {label && (
        <label htmlFor={name} className="text-sm text-gray-300">
          {label}
        </label>
      )}
      <input
        type={type}
        name={name}
        id={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        className="w-full px-4 py-2 bg-[#1C2433] border border-gray-600 rounded-md text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
  );
}
