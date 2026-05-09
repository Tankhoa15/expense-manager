import { useState, useEffect } from 'react'
import { dashboardService } from '../services/api'
import { TrendingUp, TrendingDown, Wallet, Clock } from 'lucide-react'

export default function Dashboard() {
  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDashboard()
  }, [])

  const loadDashboard = async () => {
    try {
      const res = await dashboardService.get()
      setDashboard(res.data.data)
    } catch (err) {
      console.error('Failed to load dashboard:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount || 0)
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="card p-6">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-indigo-100 rounded-full">
              <Wallet className="text-indigo-600" size={24} />
            </div>
            <div>
              <p className="text-sm text-gray-500">Total Balance</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(dashboard?.totalBalance)}
              </p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-green-100 rounded-full">
              <TrendingUp className="text-green-600" size={24} />
            </div>
            <div>
              <p className="text-sm text-gray-500">Monthly Income</p>
              <p className="text-2xl font-bold text-green-600">
                {formatCurrency(dashboard?.monthIncome)}
              </p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-red-100 rounded-full">
              <TrendingDown className="text-red-600" size={24} />
            </div>
            <div>
              <p className="text-sm text-gray-500">Monthly Expense</p>
              <p className="text-2xl font-bold text-red-600">
                {formatCurrency(dashboard?.monthExpense)}
              </p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-yellow-100 rounded-full">
              <Clock className="text-yellow-600" size={24} />
            </div>
            <div>
              <p className="text-sm text-gray-500">Pending</p>
              <p className="text-2xl font-bold text-yellow-600">
                {formatCurrency(dashboard?.pendingAmount)}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Money Sources */}
        <div className="card p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Money Sources</h2>
          <div className="space-y-3">
            {dashboard?.moneySources?.length > 0 ? (
              dashboard.moneySources.map((source) => (
                <div key={source.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center">
                      <Wallet size={20} className="text-indigo-600" />
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{source.name}</p>
                      <p className="text-sm text-gray-500">{source.sourceType}</p>
                    </div>
                  </div>
                  <p className="font-semibold text-gray-900">
                    {formatCurrency(source.currentBalance)}
                  </p>
                </div>
              ))
            ) : (
              <p className="text-gray-500 text-center py-8">No money sources yet</p>
            )}
          </div>
        </div>

        {/* Recent Transactions */}
        <div className="card p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Recent Transactions</h2>
          <div className="space-y-3">
            {dashboard?.recentTransactions?.length > 0 ? (
              dashboard.recentTransactions.map((tx) => (
                <div key={tx.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center gap-3">
                    <div
                      className="w-10 h-10 rounded-full flex items-center justify-center text-lg"
                      style={{ backgroundColor: tx.categoryColor + '20', color: tx.categoryColor }}
                    >
                      {tx.categoryIcon || tx.categoryName?.charAt(0)}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{tx.description}</p>
                      <p className="text-sm text-gray-500">
                        {tx.categoryName} • {tx.status}
                      </p>
                    </div>
                  </div>
                  <p className={`font-semibold ${tx.categoryName?.toLowerCase().includes('income') ? 'text-green-600' : 'text-red-600'}`}>
                    {tx.categoryName?.toLowerCase().includes('income') ? '+' : '-'}{formatCurrency(tx.amount)}
                  </p>
                </div>
              ))
            ) : (
              <p className="text-gray-500 text-center py-8">No transactions yet</p>
            )}
          </div>
        </div>
      </div>

      {/* Category Breakdown */}
      {dashboard?.categoryBreakdown?.length > 0 && (
        <div className="card p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Expense by Category</h2>
          <div className="space-y-4">
            {dashboard.categoryBreakdown.map((cat) => (
              <div key={cat.categoryId}>
                <div className="flex justify-between text-sm mb-1">
                  <span className="flex items-center gap-2">
                    <span className="text-lg">{cat.icon}</span>
                    {cat.categoryName}
                  </span>
                  <span className="font-medium">{formatCurrency(cat.totalAmount)} ({cat.percentage}%)</span>
                </div>
                <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-indigo-500 rounded-full"
                    style={{ width: `${cat.percentage}%` }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
