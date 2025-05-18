import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/button";

const STEPS = [
  {
    id: "regime",
    name: "Regime Detection",
    path: "/regime-detection",
    required: false,
    storageKey: "strategyLab_regimeData",
  },
  {
    id: "alphaSignal",
    name: "Alpha Signal Discovery",
    path: "/alpha-signal",
    required: true,
    storageKey: "strategyLab_alphaSignalData",
  },
  {
    id: "backtesting",
    name: "Backtesting",
    path: "/backtesting",
    required: true,
    storageKey: "strategyLab_backtestingData",
  },
  {
    id: "optimization",
    name: "Portfolio Optimization",
    path: "/portfolio-optimization",
    required: false,
    storageKey: "strategyLab_optimizationData",
  },
  {
    id: "riskAnalysis",
    name: "Risk Analysis",
    path: "/risk-analysis",
    required: false,
    storageKey: "strategyLab_riskAnalysisData",
  },
  // Options pricing removed because optional and separate module
];

function StrategyCreate() {
  const navigate = useNavigate();

  // Let's create a simple sessionId here (can be generated UUID or from route)
  // For demo, fixed sessionId
  const sessionId = "session_123";

  // State holds imported data for each step keyed by step.id
  const [importedData, setImportedData] = useState({});

  // On mount, check localStorage if data already exists for each step
  useEffect(() => {
    const newImportedData = {};
    STEPS.forEach((step) => {
      const key = `${step.storageKey}_${sessionId}`;
      const dataStr = localStorage.getItem(key);
      if (dataStr) {
        try {
          newImportedData[step.id] = JSON.parse(dataStr);
        } catch {
          // invalid JSON ignore
        }
      }
    });
    setImportedData(newImportedData);
  }, [sessionId]);

  // Import data handler for a step
  const handleImportData = (step) => {
    const key = `${step.storageKey}_${sessionId}`;
    const dataStr = localStorage.getItem(key);
    if (!dataStr) {
      alert(`No exported data found for ${step.name}. Please run and export from the module first.`);
      return;
    }
    try {
      const data = JSON.parse(dataStr);
      setImportedData((prev) => ({ ...prev, [step.id]: data }));
      alert(`${step.name} data imported successfully.`);
    } catch {
      alert(`Error parsing imported data for ${step.name}.`);
    }
  };

  // Check if required steps are imported to enable proceed
  const allRequiredImported = STEPS.filter((s) => s.required).every(
    (step) => importedData[step.id]
  );

  // Proceed to next page or final step
  const handleProceed = () => {
    if (!allRequiredImported) {
      alert("Please import data for all required steps before proceeding.");
      return;
    }
    // Navigate to Review and Tune step or summary page
    navigate("/strategy-lab/create/review");
  };

  return (
    <div className="min-h-screen bg-[#0B1120] text-white px-6 md:px-24 py-10 font-sans">
      <h1 className="text-4xl font-bold mb-8 text-center">Create New Strategy</h1>

      {STEPS.map((step) => (
        <div
          key={step.id}
          className="bg-[#1C2433] p-6 rounded-xl shadow-lg mb-6 flex flex-col md:flex-row md:items-center md:justify-between"
        >
          <div>
            <h2 className="text-2xl font-semibold">{step.name}</h2>
            <p className="text-gray-400">
              {step.required ? "Required step" : "Optional step"}
            </p>
            <p className="mt-2">
              Status:{" "}
              {importedData[step.id] ? (
                <span className="text-green-400 font-semibold">Data Imported</span>
              ) : (
                <span className="text-red-400 font-semibold">No data imported</span>
              )}
            </p>
          </div>

          <div className="mt-4 md:mt-0 space-x-3 flex flex-wrap">
            <Button
              onClick={() => navigate(step.path)}
              className="bg-blue-600 hover:bg-blue-700"
            >
              Go to {step.name} Module
            </Button>
            <Button
              onClick={() => handleImportData(step)}
              disabled={!!importedData[step.id]}
              className={`${
                importedData[step.id]
                  ? "bg-gray-600 cursor-not-allowed"
                  : "bg-green-600 hover:bg-green-700"
              }`}
            >
              Import Data
            </Button>
          </div>
        </div>
      ))}

      <div className="text-center mt-10">
        <Button
          onClick={handleProceed}
          disabled={!allRequiredImported}
          className={`px-8 py-3 text-xl rounded-xl shadow-lg transition-transform hover:scale-105 ${
            !allRequiredImported
              ? "bg-gray-600 cursor-not-allowed"
              : "bg-gradient-to-r from-green-600 to-blue-600"
          }`}
        >
          Proceed to Review & Tune
        </Button>
      </div>
    </div>
  );
}

export default StrategyCreate;
