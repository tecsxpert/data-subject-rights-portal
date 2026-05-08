// Lightweight toast — no external library needed
// Usage: import { toast } from './Toast';  then  toast.error('msg') / toast.success('msg')
// Place <Toaster /> once at the app root.

import { useState, useEffect } from 'react';

let _id = 0;
const listeners = new Set();

function emit(type, message) {
  const item = { id: ++_id, type, message };
  listeners.forEach(fn => fn(item));
}

export const toast = {
  success: msg => emit('success', msg),
  error:   msg => emit('error',   msg),
  info:    msg => emit('info',    msg),
};

const COLOURS = {
  success: 'bg-green-600',
  error:   'bg-red-600',
  info:    'bg-blue-600',
};

export function Toaster() {
  const [items, setItems] = useState([]);

  useEffect(() => {
    const handler = item => {
      setItems(prev => [...prev, item]);
      setTimeout(() => setItems(prev => prev.filter(t => t.id !== item.id)), 3500);
    };
    listeners.add(handler);
    return () => listeners.delete(handler);
  }, []);

  if (!items.length) return null;

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
      {items.map(item => (
        <div key={item.id}
          className={`${COLOURS[item.type]} text-white px-4 py-3 rounded-lg shadow-lg text-sm max-w-xs`}>
          {item.message}
        </div>
      ))}
    </div>
  );
}
