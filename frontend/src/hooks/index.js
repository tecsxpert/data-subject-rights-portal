import { useState, useEffect, useCallback } from 'react';

// src/hooks/useDebounce.js
export function useDebounce(value, delay = 400) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(id);
  }, [value, delay]);
  return debounced;
}

// src/hooks/useDsrRequests.js — re-export from same file for convenience
import { dsrApi } from '../services/api';
import { toast } from '../components/ui/Toast';

export function useDsrList(initialParams = {}) {
  const [data,    setData]    = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  const fetch = useCallback(async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const result = await dsrApi.search({ ...initialParams, ...params });
      setData(result);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to load requests';
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  return { data, loading, error, fetch };
}

export function useDsrStats() {
  const [stats,   setStats]   = useState(null);
  const [loading, setLoading] = useState(false);

  const fetch = useCallback(async () => {
    setLoading(true);
    try {
      const result = await dsrApi.getStats();
      setStats(result);
    } finally {
      setLoading(false);
    }
  }, []);

  return { stats, loading, fetch };
}
