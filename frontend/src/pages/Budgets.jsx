import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, X, AlertTriangle } from 'lucide-react'
import { budgetService, categoryService } from '../services/api'

const EMPTY_FORM = {
  amount: '',
  period: 'MONTHLY',
  periodStart: new Date().toISOString().split('T')[0].slice(0, 7) + '-01',
  periodEnd: '',
  categoryId: '',
  alertThreshold: 80,
}

export default function Budgets() {
  const [budgets, setBudgets] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [formData, setFormData] = useState(EMPTY_FORM)
  const [error, setError] = useState('')

  useEffect(() => {
    loadData()
  }, [])

  useEffect(() => {
    const now = new Date()
    const defaultEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0]
    if (!formData.periodEnd) {
      setFormData(prev => ({ ...prev, periodEnd: defaultEnd }))
    }
  }, [formData.period])

  const loadData = async () => {
    setLoading(true)
    try {
      const [budgetsRes, categoriesRes] = await Promise.all([
        budgetService.getAll(),
        categoryService.getAll().catch(() => ({ data: { data: [] } })),
      ])
      setBudgets(budgetsRes.data.data || [])
      setCategories(categoriesRes.data.data || [])
    } catch (err) {
      console.error('Failed to load budgets:', err)
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
        alertThreshold: parseInt(formData.alertThreshold),
      }

      if (editingId) {
        await budgetService.update(editingId, data)
      } else {
        await budgetService.create(data)
      }

      setShowModal(false)
      setEditingId(null)
      setFormData(EMPTY_FORM)
      loadData()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save budget')
    }
  }

  const handleEdit = (budget) => {
    setEditingId(budget.id)
    setFormData({
      amount: budget.amount.toString(),
      period: budget.period,
      periodStart: budget.periodStart,
      periodEnd: budget.periodEnd,
      categoryId: budget.categoryId || '',
      alertThreshold: budget.alertThreshold || 80,
    })
    setShowModal(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this budget?')) return
    try {
      await budgetService.delete(id)
      loadData()
    } catch (err) {
      alert('Failed to delete budget')
    }
  }

  const openCreateModal = () => {
    const now = new Date()
    const defaultEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0]
    setEditingId(null)
    setFormData({ ...EMPTY_FORM, periodEnd: defaultEnd })
    setError('')
    setShowModal(true)
  }

  const getProgressColor = (percentage) => {
    if (percentage >= 100) return 'bg-red-500'
    if (percentage >= 80) return 'bg-yellow-500'
    return 'bg-green-500'
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Budgets</h1>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2">
          <Plus size={20} />
          Add Budget
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {budgets.length > 0 ? (
            budgets.map((budget) => {
              const percentage = budget.percentageUsed || 0
              const isOverBudget = percentage >= 100
              const isWarning = percentage >= (budget.alertThreshold || 80)

              return (
                <div key={budget.id} className="card p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div>
                      <h3 className="font-semibold text-gray-900">{budget.categoryName || 'Overall Budget'}</h3>
                      <p className="text-sm text-gray-500">{budget.period}</p>
                    </div>
                    <div className="flex gap-2">
                      <button onClick={() => handleEdit(budget)} className="p-2 text-gray-400 hover:text-indigo-600">
                        <Pencil size={16} />
                      </button>
                      <button onClick={() => handleDelete(budget.id)} className="p-2 text-gray-400 hover:text-red-600">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </div>

                  <div className="mb-4">
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-gray-600">
                        ${budget.spentAmount?.toFixed(2) || '0.00'} / ${budget.amount?.toFixed(2) || '0.00'}
                      </span>
                      <span className={`font-medium ${isOverBudget ? 'text-red-600' : 'text-gray-600'}`}>
                        {percentage}%
                      </span>
                    </div>
                    <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
                      <div
                        className={`h-full ${getProgressColor(percentage)} transition-all`}
                        style={{ width: `${Math.min(percentage, 100)}%` }}
                      ></div>
                    </div>
                  </div>

                  {isWarning && (
                    <div className={`flex items-center gap-2 text-sm ${isOverBudget ? 'text-red-600' : 'text-yellow-600'}`}>
                      <AlertTriangle size={16} />
                      <span>{isOverBudget ? 'Budget exceeded!' : 'Approaching budget limit'}</span>
                    </div>
                  )}

                  <p className="text-sm text-gray-500 mt-2">
                    {budget.periodStart} - {budget.periodEnd}
                  </p>
                </div>
              )
            })
          ) : (
            <div className="col-span-full text-center py-12 text-gray-500">
              No budgets set yet
            </div>
          )}
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold">{editingId ? 'Edit Budget' : 'New Budget'}</h2>
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
                  required
                />
              </div>

              <div>
                <label className="label">Period</label>
                <select
                  className="input"
                  value={formData.period}
                  onChange={(e) => setFormData({ ...formData, period: e.target.value })}
                >
                  <option value="DAILY">Daily</option>
                  <option value="WEEKLY">Weekly</option>
                  <option value="MONTHLY">Monthly</option>
                  <option value="YEARLY">Yearly</option>
                </select>
              </div>

              <div>
                <label className="label">Category (optional)</label>
                <select
                  className="input"
                  value={formData.categoryId}
                  onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                >
                  <option value="">All Categories</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">Start Date</label>
                  <input
                    type="date"
                    className="input"
                    value={formData.periodStart}
                    onChange={(e) => setFormData({ ...formData, periodStart: e.target.value })}
                    required
                  />
                </div>
                <div>
                  <label className="label">End Date</label>
                  <input
                    type="date"
                    className="input"
                    value={formData.periodEnd}
                    onChange={(e) => setFormData({ ...formData, periodEnd: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div>
                <label className="label">Alert Threshold (%)</label>
                <input
                  type="number"
                  min="1"
                  max="100"
                  className="input"
                  value={formData.alertThreshold}
                  onChange={(e) => setFormData({ ...formData, alertThreshold: e.target.value })}
                />
                <p className="text-xs text-gray-500 mt-1">Get notified when spending reaches this percentage</p>
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
