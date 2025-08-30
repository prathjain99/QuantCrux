import React from 'react';
import { TrendingUp } from 'lucide-react';

const LoadingSpinner: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
      <div className="text-center">
        <div className="flex items-center justify-center mb-4 animate-pulse">
          <TrendingUp className="w-12 h-12 text-emerald-400 mr-3 animate-bounce" />
          <h1 className="text-3xl font-bold bg-gradient-to-r from-emerald-400 to-blue-400 bg-clip-text text-transparent">
            QuantCrux
          </h1>
        </div>
        <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto"></div>
        <p className="text-slate-400 mt-4">Loading your workspace...</p>
      </div>
    </div>
  );
};

export default LoadingSpinner;