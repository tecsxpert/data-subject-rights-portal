import React, { useState } from 'react';
import api from '../services/api';

const CreateRequest = () => {
  const [formData, setFormData] = useState({ requester_name: '', email: '', request_type: '' });
  const [errors, setErrors] = useState({});

  const validate = () => {
    let tempErrors = {};
    if (!formData.requester_name) tempErrors.name = "Name is required";
    if (!formData.email.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/)) tempErrors.email = "Invalid email format";
    if (!formData.request_type) tempErrors.type = "Please select a request type";
    setErrors(tempErrors);
    return Object.keys(tempErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (validate()) {
      try {
        await api.post('/requests/create', formData);
        alert("Request Created!");
      } catch (err) {
        alert("Backend error: " + err.response?.data?.message);
      }
    }
  };

  return (
    <div className="max-w-md mx-auto p-6 bg-white shadow-lg rounded-lg mt-10">
      <h2 className="text-xl font-bold mb-4">New Subject Request</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <input 
            className="w-full p-2 border rounded" 
            placeholder="Requester Name"
            onChange={(e) => setFormData({...formData, requester_name: e.target.value})} 
          />
          {errors.name && <p className="text-red-500 text-xs">{errors.name}</p>}
        </div>
        <div>
          <input 
            className="w-full p-2 border rounded" 
            placeholder="Email Address"
            onChange={(e) => setFormData({...formData, email: e.target.value})} 
          />
          {errors.email && <p className="text-red-500 text-xs">{errors.email}</p>}
        </div>
        <div>
          <select 
            className="w-full p-2 border rounded"
            onChange={(e) => setFormData({...formData, request_type: e.target.value})}
          >
            <option value="">-- Select Type --</option>
            <option value="ACCESS">Access</option>
            <option value="DELETE">Delete</option>
            <option value="PORTABILITY">Portability</option>
          </select>
          {errors.type && <p className="text-red-500 text-xs">{errors.type}</p>}
        </div>
        <button type="submit" className="w-full bg-[#1B4F8A] text-white py-2 rounded font-bold">
          Submit to Portal
        </button>
      </form>
    </div>
  );
};

export default CreateRequest;