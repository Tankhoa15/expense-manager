import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, X, FolderOpen } from 'lucide-react'
import { categoryService } from '../services/api'

const CATEGORY_TYPES = [
  { value: 'INCOME', label: 'Income' },
  { value: 'EXPENSE', label: 'Expense' },
]

const PRESET_ICONS = ['🍔', '🚗', '🏠', '💊', '🎮', '📚', '💰', '✈️', '👕', '🎁', '💳', '📱']
const PRESET_COLORS = ['#ef4444', '#f97316', '#eab308', '#22c55e', '#14b8a6', '#3b82f6', '#8b5cf6', '#ec4899']

const EMPTY_FORM = {
  name: '',
  type: 'EXPENSE',
  icon: '',
  color: '#6366f1',
}

export default function Categories() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [formData, setFormData] = useState(EMPTY_FORM)
  const [error, setError] = useState('')

  useEffect(() => {
    loadCategories()
  }, [])

  const loadCategories = async () => {
    try {
      const res = await categoryService.getAll()
      setCategories(res.data.data || [])
    } catch (err) {
      console.error('Failed to load categories:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    try {
      if (editingId) {
        await categoryService.update(editingId, formData)
      } else {
        await categoryService.create(formData)
      }

      setShowModal(false)
      setEditingId(null)
      setFormData(EMPTY_FORM)
      loadCategories()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save')
    }
  }

  const handleEdit = (cat) => {
    setEditingId(cat.id)
    setFormData({
      name: cat.name,
      type: cat.type,
      icon: cat.icon || '',
      color: cat.color || '#6366f1',
    })
    setShowModal(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure?')) return
    try {
      await categoryService.delete(id)
      loadCategories()
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

  const incomeCategories = categories.filter(c => c.type === 'INCOME')
  const expenseCategories = categories.filter(c => c.type === 'EXPENSE')

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Categories</h1>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2">
          <Plus size={20} />
          Add Category
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
        </div>
      ) : (
        <>
          {/* Income Categories */}
          <div className="card p-6">
            <h2 className="text-lg font-semibold text-green-600 mb-4">Income Categories</h2>
            <div className="flex flex-wrap gap-3">
              {incomeCategories.length > 0 ? (
                incomeCategories.map((cat) => (
                  <div key={cat.id} className="flex items-center gap-2 px-4 py-2 rounded-lg" style={{ backgroundColor: cat.color + '20' }}>
                    <span className="text-lg">{cat.icon || '💰'}</span>
                    <span className="font-medium text-gray-700">{cat.name}</span>
                    <button onClick={() => handleEdit(cat)} className="p-1 hover:bg-white/50 rounded">
                      <Pencil size={14} className="text-gray-500" />
                    </button>
                    <button onClick={() => handleDelete(cat.id)} className="p-1 hover:bg-red-100 rounded">
                      <Trash2 size={14} className="text-red-500" />
                    </button>
                  </div>
                ))
              ) : (
                <p className="text-gray-500">No income categories yet</p>
              )}
            </div>
          </div>

          {/* Expense Categories */}
          <div className="card p-6">
            <h2 className="text-lg font-semibold text-red-600 mb-4">Expense Categories</h2>
            <div className="flex flex-wrap gap-3">
              {expenseCategories.length > 0 ? (
                expenseCategories.map((cat) => (
                  <div key={cat.id} className="flex items-center gap-2 px-4 py-2 rounded-lg" style={{ backgroundColor: cat.color + '20' }}>
                    <span className="text-lg">{cat.icon || '📦'}</span>
                    <span className="font-medium text-gray-700">{cat.name}</span>
                    <button onClick={() => handleEdit(cat)} className="p-1 hover:bg-white/50 rounded">
                      <Pencil size={14} className="text-gray-500" />
                    </button>
                    <button onClick={() => handleDelete(cat.id)} className="p-1 hover:bg-red-100 rounded">
                      <Trash2 size={14} className="text-red-500" />
                    </button>
                  </div>
                ))
              ) : (
                <p className="text-gray-500">No expense categories yet</p>
              )}
            </div>
          </div>
        </>
      )}

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold">{editingId ? 'Edit Category' : 'New Category'}</h2>
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
                  placeholder="e.g., Food, Salary"
                  required
                />
              </div>

              <div>
                <label className="label">Type</label>
                <select
                  className="input"
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                >
                  {CATEGORY_TYPES.map((type) => (
                    <option key={type.value} value={type.value}>{type.label}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="label">Icon</label>
                <div className="flex flex-wrap gap-2">
                  {PRESET_ICONS.map((icon) => (
                    <button
                      key={icon}
                      type="button"
                      onClick={() => setFormData({ ...formData, icon })}
                      className={`w-10 h-10 text-xl rounded-lg border-2 ${
                        formData.icon === icon ? 'border-indigo-500 bg-indigo-50' : 'border-gray-200'
                      }`}
                    >
                      {icon}
                    </button>
                  ))}
                </div>
                <input
                  type="text"
                  className="input mt-2"
                  value={formData.icon}
                  onChange={(e) => setFormData({ ...formData, icon: e.target.value })}
                  placeholder="Or enter custom emoji"
                />
              </div>

              <div>
                <label className="label">Color</label>
                <div className="flex flex-wrap gap-2">
                  {PRESET_COLORS.map((color) => (
                    <button
                      key={color}
                      type="button"
                      onClick={() => setFormData({ ...formData, color })}
                      className={`w-8 h-8 rounded-full border-2 ${
                        formData.color === color ? 'border-gray-900' : 'border-transparent'
                      }`}
                      style={{ backgroundColor: color }}
                    />
                  ))}
                </div>
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
