import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Eye, EyeOff, TrendingUp, Lock, Mail } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import toast, { Toaster } from 'react-hot-toast';

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await login({ email, password });
      toast.success('Login successful!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const demoCredentials = [
    { email: 'admin@quantcrux.com', role: 'Admin', password: 'password' },
    { email: 'john.pm@quantcrux.com', role: 'Portfolio Manager', password: 'password' },
    { email: 'alice.research@quantcrux.com', role: 'Researcher', password: 'password' },
    { email: 'bob.client@quantcrux.com', role: 'Client', password: 'password' }
  ];

  const quickLogin = (demoEmail: string, demoPassword: string) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center p-4">
      <Toaster position="top-right" />
      
      <div className="w-full max-w-6xl grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Left Side - Branding */}
        <div className="hidden lg:flex flex-col justify-center items-center text-white p-12">
          <div className="text-center mb-8">
            <div className="flex items-center justify-center mb-6">
              <TrendingUp className="w-12 h-12 text-emerald-400 mr-3" />
              <h1 className="text-4xl font-bold bg-gradient-to-r from-emerald-400 to-blue-400 bg-clip-text text-transparent">
                QuantCrux
              </h1>
            </div>
            <p className="text-xl text-slate-300 leading-relaxed">
              Professional quantitative finance platform for strategy development, backtesting, and portfolio management
            </p>
          </div>
          
          <div className="grid grid-cols-2 gap-4 w-full max-w-md">
            <div className="bg-slate-800/50 backdrop-blur-sm rounded-lg p-4 border border-slate-700">
              <h3 className="text-emerald-400 font-semibold mb-1">Strategy Builder</h3>
              <p className="text-sm text-slate-400">Create quantitative trading strategies</p>
            </div>
            <div className="bg-slate-800/50 backdrop-blur-sm rounded-lg p-4 border border-slate-700">
              <h3 className="text-blue-400 font-semibold mb-1">Risk Analytics</h3>
              <p className="text-sm text-slate-400">Advanced portfolio risk modeling</p>
            </div>
            <div className="bg-slate-800/50 backdrop-blur-sm rounded-lg p-4 border border-slate-700">
              <h3 className="text-amber-400 font-semibold mb-1">Backtesting</h3>
              <p className="text-sm text-slate-400">Historical strategy simulation</p>
            </div>
            <div className="bg-slate-800/50 backdrop-blur-sm rounded-lg p-4 border border-slate-700">
              <h3 className="text-purple-400 font-semibold mb-1">Trade Execution</h3>
              <p className="text-sm text-slate-400">Professional trading interface</p>
            </div>
          </div>
        </div>

        {/* Right Side - Login Form */}
        <div className="flex flex-col justify-center p-8">
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-2xl border border-slate-700 p-8 shadow-2xl">
            <div className="text-center mb-8">
              <div className="lg:hidden flex items-center justify-center mb-4">
                <TrendingUp className="w-8 h-8 text-emerald-400 mr-2" />
                <h1 className="text-2xl font-bold bg-gradient-to-r from-emerald-400 to-blue-400 bg-clip-text text-transparent">
                  QuantCrux
                </h1>
              </div>
              <h2 className="text-2xl font-bold text-white mb-2">Welcome Back</h2>
              <p className="text-slate-400">Sign in to your account</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    placeholder="Enter your email"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Password
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full pl-10 pr-12 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    placeholder="Enter your password"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-slate-400 hover:text-slate-300 transition-colors"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-4 rounded-lg transition-all duration-200 transform hover:scale-[1.02] disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
              >
                {loading ? 'Signing In...' : 'Sign In'}
              </button>
            </form>

            <div className="mt-6 text-center">
              <p className="text-slate-400">
                Don't have an account?{' '}
                <Link 
                  to="/register" 
                  className="text-emerald-400 hover:text-emerald-300 font-medium transition-colors"
                >
                  Sign up
                </Link>
              </p>
            </div>

            {/* Demo Credentials */}
            <div className="mt-8 p-4 bg-slate-800/30 rounded-lg border border-slate-700">
              <h3 className="text-sm font-medium text-slate-300 mb-3">Demo Credentials</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
                {demoCredentials.map((cred, index) => (
                  <button
                    key={index}
                    onClick={() => quickLogin(cred.email, cred.password)}
                    className="text-left p-2 bg-slate-800/50 rounded border border-slate-600 hover:border-emerald-500 transition-colors"
                  >
                    <div className="text-emerald-400 font-medium">{cred.role}</div>
                    <div className="text-slate-400 truncate">{cred.email}</div>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;