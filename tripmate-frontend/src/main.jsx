import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'

// StrictMode is intentionally omitted — react-beautiful-dnd is incompatible with
// React 18 double-invocation in StrictMode.
ReactDOM.createRoot(document.getElementById('root')).render(<App />)
