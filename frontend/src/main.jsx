import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import Home from './pages/home'

// 🔐 Auth Pages
import Register from './pages/auth/Register'
import Login from './pages/auth/Login'
import ForgotPassword from './pages/auth/ForgotPassword'


// ⚠️ System Pages
import NotFound from './pages/system/NotFound';
import ServerError from './pages/system/ServerError';
import Unauthorized from './pages/system/Unauthorized';
import Maintenance from './pages/system/Maintenance';

import './index.css'


ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/register" element={<Register />} />
        <Route path="/log-in" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        <Route path="/500" element={<ServerError />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="/maintenance" element={<Maintenance />} />
        <Route path="*" element={<NotFound />} />
        {/* You can add more routes later */}
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
)
