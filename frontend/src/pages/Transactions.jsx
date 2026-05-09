import { useState, useEffect } from 'react'
import { Plus, Search, Pencil, Trash2, X, Check, XCircle, ChevronLeft, ChevronRight, AlertCircle, TrendingUp, TrendingDown } from 'lucide-react'
import { transactionService, categoryService, moneySourceService } from '../services/api'

const EMPTY_FORM = {
  amount: '',
  description: '',
  transactionDate: new Date().toISOString().split('T')[0],
  moneySourceId: '',
  categoryId: '',
  note: '',
}

export default function Transactions() {
  const [transactions, setTransactions] = useState({ content: [], totalPages: 0, totalElements: 0 })
  const [pendingTransactions, setPendingTransactions] = useState([])
  const [categories, setCategories] = useState([])
  const [moneySources, setMoneySources] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [showModal, setShowModal] = useState(false)
  const [showConfirmModal, setShowConfirmModal] = useState(false)
  const [selectedTransaction, setSelectedTransaction] = useState(null)
  const [editingId, setEditingId] = useState(null)
  const [formData, setFormData] = useState(EMPTY_FORM)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState('all')

  useEffect(() => {
    loadData()
  }, [page, filter])

  const loadData = async () => {
    setLoading(true)
    try {
      const [catRes, sourceRes] = await Promise.all([
        categoryService.getAll(),
        moneySourceService.getAll(),
      ])
      setCategories(catRes.data.data || [])
      setMoneySources(sourceRes.data.data || [])

      if (filter === 'pending') {
        const pendingRes = await transactionService.getPending(page, 20)
        setPendingTransactions(pendingRes.data.data.content || [])
      } else {
        const res = await transactionService.getAll(page, 20)
        setTransactions(res.data.data)
      }
    } catch (err) {
      console.error('Failed to load data:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    try {
      const data = {
        ...formData,
        amount: parseFloat(formData.amount),
        moneySourceId: parseInt(formData.moneySourceId),
        categoryId: parseInt(formData.categoryId),
      }

      if (editingId) {
        await transactionService.delete(editingId)
      }
      await transactionService.create(data)

      setShowModal(false)
      setEditingId(null)
      setFormData(EMPTY_FORM)
      loadData()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save transaction')
    }
  }

  const handleConfirm = async (tx) => {
    setSelectedTransaction(tx)
    setShowConfirmModal(true)
  }

  const confirmTransaction = async () => {
    try {
      await transactionService.confirm(selectedTransaction.id)
      setShowConfirmModal(false)
      setSelectedTransaction(null)
      loadData()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to confirm transaction')
    }
  }

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this transaction?')) return
    try {
      await transactionService.cancel(id)
      loadData()
    } catch (err) {
      alert('Failed to cancel transaction')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this transaction?')) return
    try {
      await transactionService.delete(id)
      loadData()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete transaction')
    }
  }

  const openCreateModal = () => {
    setEditingId(null)
    setFormData(EMPTY_FORM)
    setError('')
    setShowModal(true)
  }

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount || 0)
  }

  const displayTransactions = filter === 'pending' ? pendingTransactions : transactions.content || []

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Transactions</h1>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2">
          <Plus size={20} />
          New Transaction
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="card p-1 inline-flex">
        <button
          onClick={() => { setFilter('all'); setPage(0); }}
          className={`px-4 py-2 rounded-lg font-medium transition-colors ${
            filter === 'all' ? 'bg-indigo-600 text-white' : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          All
        </button>
        <button
          onClick={() => { setFilter('pending'); setPage(0); }}
          className={`px-4 py-2 rounded-lg font-medium transition-colors flex items-center gap-2 ${
            filter === 'pending' ? 'bg-indigo-600 text-white' : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          Pending
          {pendingTransactions.length > 0 && (
            <span className="bg-yellow-500 text-white text-xs px-2 py-0.5 rounded-full">
              {pendingTransactions.length}
            </span>
          )}
        </button>
      </div>

      {/* Transactions Table */}
      <div className="card overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
          </div>
        ) : (
          <>
            <table className="w-full">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Description</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Category</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Source</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Amount</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {displayTransactions.length > 0 ? (
                  displayTransactions.map((tx) => (
                    <tr key={tx.id} className={`hover:bg-gray-50 ${tx.status === 'PENDING' ? 'bg-yellow-50' : ''}`}>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                          tx.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' :
                          tx.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                          'bg-gray-100 text-gray-700'
                        }`}>
                          {tx.status === 'CONFIRMED' ? <Check size={12} className="mr-1" /> :
                           tx.status === 'PENDING' ? <AlertCircle size={12} className="mr-1" /> :
                           <XCircle size={12} className="mr-1" />}
                          {tx.status}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <p className="font-medium text-gray-900">{tx.description}</p>
                        {tx.note && <p className="text-sm text-gray-500">{tx.note}</p>}
                      </td>
                      <td className="px-6 py-4">
                        <span className="flex items-center gap-2">
                          <span className="text-lg">{tx.categoryIcon || '📦'}</span>
                          {tx.categoryName}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-gray-600">{tx.moneySourceName}</td>
                      <td className="px-6 py-4 text-gray-600">{tx.transactionDate}</td>
                      <td className={`px-6 py-4 text-right font-semibold ${
                        tx.categoryName?.toLowerCase().includes('income') ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {tx.categoryName?.toLowerCase().includes('income') ? '+' : '-'}{formatCurrency(tx.amount)}
                      </td>
                      <td className="px-6 py-4 text-right">
                        {tx.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => handleConfirm(tx)}
                              className="p-2 hover:bg-green-100 rounded-lg text-green-600"
                              title="Confirm"
                            >
                              <Check size={16} />
                            </button>
                            <button
                              onClick={() => handleCancel(tx.id)}
                              className="p-2 hover:bg-yellow-100 rounded-lg text-yellow-600"
                              title="Cancel"
                            >
                              <XCircle size={16} />
                            </button>
                          </>
                        )}
                        <button
                          onClick={() => handleDelete(tx.id)}
                          className="p-2 hover:bg-red-100 rounded-lg text-red-600"
                          title="Delete"
                        >
                          <Trash2 size={16} />
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" className="px-6 py-12 text-center text-gray-500">
                      No transactions found
                    </td>
                  </tr>
                )}
              </tbody>
            </table>

            {/* Pagination */}
            {filter === 'all' && transactions.totalPages > 1 && (
              <div className="px-6 py-4 border-t flex items-center justify-between">
                <p className="text-sm text-gray-600">
                  Page {page + 1} of {transactions.totalPages}
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="btn-secondary"
                  >
                    <ChevronLeft size={16} />
                  </button>
                  <button
                    onClick={() => setPage(p => p + 1)}
                    disabled={page >= transactions.totalPages - 1}
                    className="btn-secondary"
                  >
                    <ChevronRight size={16} />
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Create Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold">New Transaction</h2>
              <button onClick={() => setShowModal(false)} className="p-2 hover:bg-gray-100 rounded-lg">
                <X size={20} />
              </button>
            </div>

            {error && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="label">Amount</label>
                <input
                  type="number"
                  step="0.01"
                  className="input"
                  value={formData.amount}
                  onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                  placeholder="0.00"
                  required
                />
              </div>

              <div>
                <label className="label">Description</label>
                <input
                  type="text"
                  className="input"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="What was this for?"
                  required
                />
              </div>

              <div>
                <label className="label">Category</label>
                <select
                  className="input"
                  value={formData.categoryId}
                  onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                  required
                >
                  <option value="">Select category</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.icon} {cat.name} ({cat.type})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="label">Money Source</label>
                <select
                  className="input"
                  value={formData.moneySourceId}
                  onChange={(e) => setFormData({ ...formData, moneySourceId: e.target.value })}
                  required
                >
                  <option value="">Select source</option>
                  {moneySources.map((source) => (
                    <option key={source.id} value={source.id}>
                      {source.name} - Balance: {formatCurrency(source.currentBalance)}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="label">Date</label>
                <input
                  type="date"
                  className="input"
                  value={formData.transactionDate}
                  onChange={(e) => setFormData({ ...formData, transactionDate: e.target.value })}
                  required
                />
              </div>

              <div>
                <label className="label">Note (optional)</label>
                <input
                  type="text"
                  className="input"
                  value={formData.note}
                  onChange={(e) => setFormData({ ...formData, note: e.target.value })}
                  placeholder="Additional details..."
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button type="button" onClick={() => setShowModal(false)} className="btn-secondary flex-1">
                  Cancel
                </button>
                <button type="submit" className="btn-primary flex-1">
                  Create (Pending)
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Confirmation Modal */}
      {showConfirmModal && selectedTransaction && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="text-center mb-6">
              <div className="w-16 h-16 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <AlertCircle size={32} className="text-indigo-600" />
              </div>
              <h2 className="text-xl font-semibold mb-2">Confirm Transaction</h2>
              <p className="text-gray-600">This will update your money source balance.</p>
            </div>

            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <div className="flex justify-between mb-2">
                <span className="text-gray-600">Amount:</span>
                <span className={`font-bold ${
                  selectedTransaction.categoryName?.toLowerCase().includes('income') ? 'text-green-600' : 'text-red-600'
                }`}>
                  {selectedTransaction.categoryName?.toLowerCase().includes('income') ? '+' : '-'}{formatCurrency(selectedTransaction.amount)}
                </span>
              </div>
              <div className="flex justify-between mb-2">
                <span className="text-gray-600">Description:</span>
                <span className="font-medium">{selectedTransaction.description}</span>
              </div>
              <div className="flex justify-between mb-2">
                <span className="text-gray-600">Money Source:</span>
                <span>{selectedTransaction.moneySourceName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Date:</span>
                <span>{selectedTransaction.transactionDate}</span>
              </div>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
              <p className="text-blue-800 text-sm">
                {selectedTransaction.categoryName?.toLowerCase().includes('income') ? (
                  <>This income of <strong>{formatCurrency(selectedTransaction.amount)}</strong> will be added to <strong>{selectedTransaction.moneySourceName}</strong>.</>
                ) : (
                  <>This expense of <strong>{formatCurrency(selectedTransaction.amount)}</strong> will be deducted from <strong>{selectedTransaction.moneySourceName}</strong>.</>
                )}
              </p>
            </div>

            <div className="flex gap-3">
              <button onClick={() => setShowConfirmModal(false)} className="btn-secondary flex-1">
                Go Back
              </button>
              <button onClick={confirmTransaction} className="btn-success flex-1">
                Confirm & Update Balance
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
