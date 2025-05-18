// src/components/ExportButtons.jsx
import React from "react";
import { Button } from "@/components/button";

export default function ExportButtons({ formData, sessionId = "defaultSession" }) {
  const handleDownloadPDF = () => {
    // TODO: Implement PDF generation and download logic
    alert("PDF Download feature coming soon.");
  };

  const handleExportDashboard = () => {
    // TODO: Implement export to dashboard functionality
    alert("Export to Dashboard feature coming soon.");
  };

  const handleSaveSessionData = () => {
    const resultsData = {
      formData,
      timestamp: new Date().toISOString(),
    };
    localStorage.setItem(
      `strategyLab_sessionData_${sessionId}`,
      JSON.stringify(resultsData)
    );
    alert("Session data saved successfully. You can import it in Strategy Lab.");
  };

  return (
    <div className="flex justify-center gap-6">
      <Button
        onClick={handleDownloadPDF}
        className="bg-indigo-600 hover:bg-indigo-700 px-6 py-3 rounded-lg text-white"
      >
        Download Results as PDF
      </Button>

      <Button
        onClick={handleExportDashboard}
        className="bg-green-600 hover:bg-green-700 px-6 py-3 rounded-lg text-white"
      >
        Export to Dashboard
      </Button>

      <Button
        onClick={handleSaveSessionData}
        className="bg-yellow-600 hover:bg-yellow-700 px-6 py-3 rounded-lg text-white"
      >
        Save Session Data
      </Button>
    </div>
  );
}
