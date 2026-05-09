import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, X, Wallet } from 'lucide-react'
import { moneySourceService } from '../services/api'

const SOURCE_TYPES = [
  { value: 'CASH', label: 'Cash' },
  { value: 'BANK_ACCOUNT', label: 'Bank Account' },
  { value: 'CREDIT_CARD', label: 'Credit Card' },
  { value: 'E_WALLET', label: 'E-Wallet' },
  { value: 'OTHER', label: 'Other' },
]

const EMPTY_FORM = {
  name: '',
  sourceType: 'CASH',
  initialBalance: '',
}

export default function MoneySources() {
  const [sources, setSources] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [formData, setFormData] = useState(EMPTY_FORM)
  const [error, setError] = useState('')

  useEffect(() => {
    loadSources()
  }, [])

  const loadSources = async () => {
    try {
      const res = await moneySourceService.getAll()
      setSources(res.data.data || [])
    } catch (err) {
      console.error('Failed to load sources:', err)
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
        initialBalance: parseFloat(formData.initialBalance) || 0,
      }

      if (editingId) {
        await moneySourceService.update(editingId, data)
      } else {
        await moneySourceService.create(data)
      }

      setShowModal(false)
      setEditingId(null)
      setFormData(EMPTY_FORM)
      loadSources()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save')
    }
  }

  const handleEdit = (source) => {
    setEditingId(source.id)
    setFormData({
      name: source.name,
      sourceType: source.sourceType,
      initialBalance: source.initialBalance?.toString() || '0',
    })
    setShowModal(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure?')) return
    try {
      await moneySourceService.delete(id)
      loadSources()
    } catch (err) {
      alert('Failed to delete')
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

  const getTotalBalance = () => {
    return sources.reduce((sum, s) => sum + (s.currentBalance || 0), 0)
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Money Sources</h1>
          <p className="text-gray-500 mt-1">Total: {formatCurrency(getTotalBalance())}</p>
        </div>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2">
          <Plus size={20} />
          Add Source
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {sources.map((source) => (
            <div key={source.id} className="card p-6">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center">
                    <Wallet className="text-indigo-600" size={24} />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">{source.name}</h3>
                    <p className="text-sm text-gray-500">{source.sourceType}</p>
                  </div>
                </div>
                <div className="flex gap-1">
                  <button onClick={() => handleEdit(source)} className="p-2 hover:bg-gray-100 rounded-lg">
                    <Pencil size={16} className="text-gray-500" />
                  </button>
                  <button onClick={() => handleDelete(source.id)} className="p-2 hover:bg-red-50 rounded-lg">
                    <Trash2 size={16} className="text-red-500" />
                  </button>
                </div>
              </div>
              <div className="mt-4 pt-4 border-t">
                <p className="text-sm text-gray-500">Current Balance</p>
                <p className="text-2xl font-bold text-gray-900">{formatCurrency(source.currentBalance)}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold">{editingId ? 'Edit Source' : 'New Money Source'}</h2>
              <button onClick={() => setShowModal(false)} className="p-2 hover:bg-gray-100 rounded-lg">
                <X size={20} />
              </button>
            </div>

            {error && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">{error}</div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="label">Name</label>
                <input
                  type="text"
                  className="input"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="e.g., Main Bank Account"
                  required
                />
              </div>

              <div>
                <label className="label">Type</label>
                <select
                  className="input"
                  value={formData.sourceType}
                  onChange={(e) => setFormData({ ...formData, sourceType: e.target.value })}
                >
                  {SOURCE_TYPES.map((type) => (
                    <option key={type.value} value={type.value}>{type.label}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="label">Initial Balance</label>
                <input
                  type="number"
                  step="0.01"
                  className="input"
                  value={formData.initialBalance}
                  onChange={(e) => setFormData({ ...formData, initialBalance: e.target.value })}
                  placeholder="0.00"
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button type="button" onClick={() => setShowModal(false)} className="btn-secondary flex-1">
                  Cancel
                </button>
                <button type="submit" className="btn-primary flex-1">
                  {editingId ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
