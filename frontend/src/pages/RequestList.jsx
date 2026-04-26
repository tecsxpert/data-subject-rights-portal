import React, { useEffect, useState } from 'react';
import api from '../services/api';

const RequestList = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/requests/all')
      .then(res => {
        setRequests(res.data.content); // Assumes Spring Pageable response
        setLoading(false);
      })
      .catch(err => console.error("Error fetching data", err));
  }, []);

  if (loading) return <div className="p-10 text-center">Loading Portal Data...</div>;

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4 text-[#1B4F8A]">Data Subject Requests</h1>
      <table className="min-w-full bg-white border border-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 border-b text-left">Requester</th>
            <th className="px-6 py-3 border-b text-left">Type</th>
            <th className="px-6 py-3 border-b text-left">Status</th>
          </tr>
        </thead>
        <tbody>
          {requests.map(req => (
            <tr key={req.id} className="hover:bg-gray-50">
              <td className="px-6 py-4 border-b">{req.requester_name}</td>
              <td className="px-6 py-4 border-b">{req.request_type}</td>
              <td className="px-6 py-4 border-b">
                <span className="px-2 py-1 rounded text-sm bg-blue-100 text-blue-800">{req.status}</span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default RequestList;