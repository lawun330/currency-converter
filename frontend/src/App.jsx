import { useState, useRef, useEffect } from 'react'
import { CURRENCIES, getFlagUrl } from './currencies'
import './App.css'

// custom dropdown with flag images
function CurrencyDropdown({ value, onChange, 'aria-label': ariaLabel }) {
  const [open, setOpen] = useState(false)
  const ref = useRef(null)
  const selected = CURRENCIES.find((c) => c.code === value) || CURRENCIES[0]

  useEffect(() => {
    function handleClickOutside(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    if (open) {
      document.addEventListener('click', handleClickOutside)
      return () => document.removeEventListener('click', handleClickOutside)
    }
  }, [open])

  return (
    <div className="currency-dropdown" ref={ref}>
      <button
        type="button"
        className="currency-select currency-dropdown-trigger"
        onClick={() => setOpen((o) => !o)}
        aria-label={ariaLabel}
        aria-expanded={open}
        aria-haspopup="listbox"
      >
        <span className="currency-option-text">
          <img
            src={getFlagUrl(selected.flagCode)}
            alt=""
            className="currency-flag"
          />
          {selected.name} ({selected.code})
        </span>
        <span className="currency-dropdown-arrow" aria-hidden>▼</span>
      </button>
      {open && (
        <ul className="currency-dropdown-list" role="listbox">
          {CURRENCIES.map((c) => (
            <li
              key={c.code}
              role="option"
              aria-selected={c.code === value}
              className={`currency-dropdown-option ${c.code === value ? 'selected' : ''}`}
              onClick={() => {
                onChange(c.code)
                setOpen(false)
              }}
            >
              <span className="currency-option-text">
                <img
                  src={getFlagUrl(c.flagCode)}
                  alt=""
                  className="currency-flag"
                />
                {c.name} ({c.code})
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

// get environment variables
const API_PORT = import.meta.env.VITE_PORT || '8080'
const API_BASE = import.meta.env.VITE_API_BASE || `http://localhost:${API_PORT}`

// main component
function App() {

  // state variables
  const [fromCurrency, setFromCurrency] = useState('USD')
  const [toCurrency, setToCurrency] = useState('MMK')
  const [amount, setAmount] = useState('') // amount to convert
  const [converted, setConverted] = useState(null) // converted amount
  const [loading, setLoading] = useState(false) // loading state
  const [error, setError] = useState(null) // error state

  // clear converted result when currency dropdowns change
  useEffect(() => {
    setConverted(null)
  }, [fromCurrency, toCurrency])

  // handle convert button click
  const handleConvert = async () => {
    const num = parseFloat(amount)
    if (Number.isNaN(num) || num < 0) return
    setError(null)
    setLoading(true)
    try {
      const url = `${API_BASE}/api/convert?from=${encodeURIComponent(fromCurrency)}&to=${encodeURIComponent(toCurrency)}&amount=${encodeURIComponent(amount)}`
      const res = await fetch(url)
      if (!res.ok) throw new Error('Conversion failed')
      const data = await res.json()
      setConverted(Number(data.result).toFixed(2))
    } catch (err) {
      setError(err.message || `Cannot reach server. Is the Java API running on ${API_BASE}?`)
      setConverted(null)
    } finally {
      setLoading(false)
    }
  }

  // handle copy result button click
  const handleCopyResult = () => {
    if (converted == null) return
    navigator.clipboard.writeText(converted)
  }

  // render the component
  return (
    <div className="converter-panel">
      <div className="row row-split">
        <div className="half">
          <label className="dropdown-label">From</label>
          <CurrencyDropdown
            value={fromCurrency}
            onChange={setFromCurrency}
            aria-label="From currency"
          />
        </div>
        <div className="half">
          <label className="dropdown-label">To</label>
          <CurrencyDropdown
            value={toCurrency}
            onChange={setToCurrency}
            aria-label="To currency"
          />
        </div>
      </div>

      <div className="row row-split">
        <div className="half">
          <label className="dropdown-label">Amount</label>
          <input
            type="number"
            className="amount-input"
            placeholder="0"
            min="0"
            step="any"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            aria-label="Amount"
          />
        </div>
        <div className="half">
          <label className="dropdown-label">Converted</label>
          <output
            className={`result-output ${converted == null ? 'result-empty' : 'result-filled'}`}
            onClick={converted != null ? handleCopyResult : undefined}
            title={converted != null ? 'Click to copy' : ''}
          >
            {converted != null ? converted : ''}
          </output>
        </div>
      </div>

      <button type="button" className="convert-btn" onClick={handleConvert} disabled={loading}>
        {loading ? 'Converting…' : 'Convert'}
      </button>

      {error && <span className="error-hint">{error}</span>}
      {converted != null && !error && (
        <span className="copy-hint">Click result to copy</span>
      )}
    </div>
  )
}

export default App
