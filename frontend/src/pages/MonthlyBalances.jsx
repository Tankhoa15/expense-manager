import { useState, useEffect } from 'react'
import { Plus, Pencil, Calendar } from 'lucide-react'
import { monthlyBalanceService } from '../services/api'

const MONTHS = [
  { value: 1, label: 'January' },
  { value: 2, label: 'February' },
  { value: 3, label: 'March' },
  { value: 4, label: 'April' },
  { value: 5, label: 'May' },
  { value: 6, label: 'June' },
  { value: 7, label: 'July' },
  { value: 8, label: 'August' },
  { value: 9, label: 'September' },
  { value: 10, label: 'October' },
  { value: 11, label: 'November' },
  { value: 12, label: 'December' },
]

export default function MonthlyBalances() {
  const [balances, setBalances] = useState([])
  const [currentBalance, setCurrentBalance] = useState(null)
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [formData, setFormData] = useState({
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    openingBalance: '',
  })
  const [error, setError] = useState('')

  useEffect(() => {
    loadBalances()
  }, [])

  const loadBalances = async () => {
    try {
      const [allRes, currentRes] = await Promise.all([
        monthlyBalanceService.getAll(),
        monthlyBalanceService.getCurrent(),
      ])
      setBalances(allRes.data.data || [])
      setCurrentBalance(currentRes.data.data)
    } catch (err) {
      console.error('Failed to load balances:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    try {
      await monthlyBalanceService.createOrUpdate({
        year: formData.year,
        month: formData.month,
        openingBalance: parseFloat(formData.openingBalance) || 0,
      })
      setShowModal(false)
      setFormData({ ...formData, openingBalance: '' })
      loadBalances()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save')
    }
  }

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount || 0)
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Monthly Balance</h1>
          <p className="text-gray-500 mt-1">Set your opening balance for each month</p>
        </div>
        <button onClick={() => setShowModal(true)} className="btn-primary flex items-center gap-2">
          <Plus size={20} />
          Set Balance
        </button>
      </div>

      {/* Current Month Summary */}
      {currentBalance && (
        <div className="card p-6 bg-gradient-to-r from-indigo-500 to-purple-500 text-white">
          <div className="flex items-center gap-4 mb-4">
            <Calendar size={32} />
            <div>
              <p className="text-sm opacity-80">{currentBalance.monthName} {currentBalance.year}</p>
              <p className="text-3xl font-bold">Current Balance: {formatCurrency(currentBalance.currentBalance)}</p>
            </div>
          </div>
          <div className="grid grid-cols-3 gap-4 mt-4">
            <div className="bg-white/20 rounded-lg p-3">
              <p className="text-sm opacity-80">Opening</p>
              <p className="text-lg font-semibold">{formatCurrency(currentBalance.openingBalance)}</p>
            </div>
            <div className="bg-white/20 rounded-lg p-3">
              <p className="text-sm opacity-80">Income</p>
              <p className="text-lg font-semibold text-green-200">{formatCurrency(currentBalance.totalIncome)}</p>
            </div>
            <div className="bg-white/20 rounded-lg p-3">
              <p className="text-sm opacity-80">Expense</p>
              <p className="text-lg font-semibold text-red-200">{formatCurrency(currentBalance.totalExpense)}</p>
            </div>
          </div>
        </div>
      )}

      {/* History */}
      <div className="card overflow-hidden">
        <div className="px-6 py-4 border-b">
          <h2 className="text-lg font-semibold">Balance History</h2>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Month</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Opening</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Income</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Expense</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Closing</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {balances.length > 0 ? (
              balances.map((balance) => (
                <tr key={balance.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-medium text-gray-900">
                    {MONTHS.find(m => m.value === balance.month)?.label} {balance.year}
                  </td>
                  <td className="px-6 py-4 text-right text-gray-600">{formatCurrency(balance.openingBalance)}</td>
                  <td className="px-6 py-4 text-right text-green-600">{formatCurrency(balance.totalIncome)}</td>
                  <td className="px-6 py-4 text-right text-red-600">{formatCurrency(balance.totalExpense)}</td>
                  <td className="px-6 py-4 text-right font-semibold text-gray-900">{formatCurrency(balance.closingBalance)}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="px-6 py-12 text-center text-gray-500">No balance records yet</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold">Set Monthly Balance</h2>
              <button onClick={() => setShowModal(false)} className="p-2 hover:bg-gray-100 rounded-lg">
                <Plus size={20} className="rotate-45" />
              </button>
            </div>

            {error && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">Year</label>
                  <select
                    className="input"
                    value={formData.year}
                    onChange={(e) => setFormData({ ...formData, year: parseInt(e.target.value) })}
                  >
                    {[2024, 2025, 2026, 2027].map((year) => (
                      <option key={year} value={year}>{year}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="label">Month</label>
                  <select
                    className="input"
                    value={formData.month}
                    onChange={(e) => setFormData({ ...formData, month: parseInt(e.target.value) })}
                  >
                    {MONTHS.map((month) => (
                      <option key={month.value} value={month.value}>{month.label}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="label">Opening Balance</label>
                <input
                  type="number"
                  step="0.01"
                  className="input"
                  value={formData.openingBalance}
                  onChange={(e) => setFormData({ ...formData, openingBalance: e.target.value })}
                  placeholder="Enter your balance at start of month"
                  required
                />
                <p className="text-xs text-gray-500 mt-1">
                  This is the balance you had at the beginning of the month (carried from previous month)
                </p>
              </div>

              <div className="flex gap-3 pt-4">
                <button type="button" onClick={() => setShowModal(false)} className="btn-secondary flex-1">
                  Cancel
                </button>
                <button type="submit" className="btn-primary flex-1">
                  Save
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
