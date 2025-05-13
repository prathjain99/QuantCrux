import React from "react";

export default function Slider({ label, min, max, step = 1, value, onChange, unit = "" }) {
  return (
    <div className="w-full mb-4">
      <label className="block text-sm text-gray-300 mb-1">
        {label} <span className="text-blue-400">({value}{unit})</span>
      </label>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full appearance-none h-2 bg-gray-700 rounded-lg outline-none transition-all focus:ring-2 focus:ring-blue-500"
      />
    </div>
  );
}
