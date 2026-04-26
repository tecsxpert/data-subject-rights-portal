import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api';

const Login = () => {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // This calls Java Developer 1's /auth/login endpoint
      const response = await api.post('/auth/login', credentials);
      const token = response.data.token;
      
      login(token); // Saves to Context & LocalStorage
      navigate('/'); // Redirects to the dashboard/list page
    } catch (err) {
      alert("Login failed. Check backend connectivity or credentials.");
    }
  };

  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      <form onSubmit={handleLogin} className="p-8 bg-white shadow-xl rounded-lg w-96">
        <h2 className="text-2xl font-bold mb-6 text-center text-[#1B4F8A]">Tool-48 Portal Login</h2>
        <input 
          type="email" 
          placeholder="Email" 
          className="w-full p-3 mb-4 border rounded"
          onChange={(e) => setCredentials({...credentials, email: e.target.value})}
          required
        />
        <input 
          type="password" 
          placeholder="Password" 
          className="w-full p-3 mb-6 border rounded"
          onChange={(e) => setCredentials({...credentials, password: e.target.value})}
          required
        />
        <button type="submit" className="w-full bg-[#1B4F8A] text-white p-3 rounded font-bold hover:bg-blue-800 transition">
          Login
        </button>
      </form>
    </div>
  );
};

export default Login;