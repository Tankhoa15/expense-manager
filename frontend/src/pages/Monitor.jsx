import { useState, useEffect } from 'react'
import { monitorService } from '../services/api'
import { Activity, TrendingUp, TrendingDown, Clock, DollarSign, BarChart3, PieChart, RefreshCw } from 'lucide-react'

export default function Monitor() {
  const [overview, setOverview] = useState(null)
  const [recentActivity, setRecentActivity] = useState([])
  const [statistics, setStatistics] = useState(null)
  const [trend, setTrend] = useState([])
  const [loading, setLoading] = useState(true)
  const [trendDays, setTrendDays] = useState(14)
  const [autoRefresh, setAutoRefresh] = useState(false)

  useEffect(() => {
    loadData()
  }, [trendDays])

  useEffect(() => {
    if (autoRefresh) {
      const interval = setInterval(loadData, 10000)
      return () => clearInterval(interval)
    }
  }, [autoRefresh])

  const loadData = async () => {
    setLoading(true)
    try {
      const [overviewRes, recentRes, statsRes, trendRes] = await Promise.all([
        monitorService.getOverview(),
        monitorService.getRecentActivity(),
        monitorService.getStatistics(),
        monitorService.getTrend(trendDays),
      ])
      setOverview(overviewRes.data.data)
      setRecentActivity(recentRes.data.data || [])
      setStatistics(statsRes.data.data)
      setTrend(trendRes.data.data || [])
    } catch (err) {
      console.error('Failed to load monitor data:', err)
    } finally {
      setLoading(false)
    }
  }

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount || 0)
  }

  const formatTime = (dateString) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    const now = new Date()
    const diff = now - date
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)

    if (seconds < 60) return `${seconds}s ago`
    if (minutes < 60) return `${minutes}m ago`
    if (hours < 24) return `${hours}h ago`
    return date.toLocaleDateString()
  }

  const maxTrendValue = Math.max(
    ...trend.map(d => Math.max(Number(d.income || 0), Number(d.expense || 0)))
  )

  if (loading && !overview) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-indigo-100 rounded-lg">
            <Activity className="text-indigo-600" size={24} />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Transaction Monitor</h1>
            <p className="text-gray-500">Real-time transaction tracking</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <label className="flex items-center gap-2 text-sm text-gray-600">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
              className="rounded border-gray-300"
            />
            Auto refresh (10s)
          </label>
          <button
            onClick={loadData}
            disabled={loading}
            className="btn-secondary flex items-center gap-2"
          >
            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
            Refresh
          </button>
        </div>
      </div>

      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="card p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Today</p>
              <p className="text-2xl font-bold">{overview?.todayCount || 0}</p>
            </div>
            <div className="p-3 bg-blue-100 rounded-full">
              <Clock className="text-blue-600" size={24} />
            </div>
          </div>
          <div className="mt-2 flex gap-4 text-sm">
            <span className="text-green-600">+{formatCurrency(overview?.todayIncome)}</span>
            <span className="text-red-600">-{formatCurrency(overview?.todayExpense)}</span>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">This Week</p>
              <p className="text-2xl font-bold">{overview?.weekCount || 0}</p>
            </div>
            <div className="p-3 bg-purple-100 rounded-full">
              <BarChart3 className="text-purple-600" size={24} />
            </div>
          </div>
          <div className="mt-2 flex gap-4 text-sm">
            <span className="text-green-600">+{formatCurrency(overview?.weekIncome)}</span>
            <span className="text-red-600">-{formatCurrency(overview?.weekExpense)}</span>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">This Month</p>
              <p className="text-2xl font-bold">{overview?.monthCount || 0}</p>
            </div>
            <div className="p-3 bg-green-100 rounded-full">
              <DollarSign className="text-green-600" size={24} />
            </div>
          </div>
          <div className="mt-2 flex gap-4 text-sm">
            <span className="text-green-600">+{formatCurrency(overview?.monthIncome)}</span>
            <span className="text-red-600">-{formatCurrency(overview?.monthExpense)}</span>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Pending</p>
              <p className="text-2xl font-bold text-yellow-600">{overview?.totalPending || 0}</p>
            </div>
            <div className="p-3 bg-yellow-100 rounded-full">
              <Clock className="text-yellow-600" size={24} />
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Transaction Trend Chart */}
        <div className="lg:col-span-2 card p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold">Transaction Trend</h2>
            <select
              value={trendDays}
              onChange={(e) => setTrendDays(Number(e.target.value))}
              className="input w-auto"
            >
              <option value={7}>Last 7 days</option>
              <option value={14}>Last 14 days</option>
              <option value={30}>Last 30 days</option>
            </select>
          </div>

          <div className="h-64 flex items-end gap-1">
            {trend.map((day, index) => {
              const incomeHeight = maxTrendValue > 0 ? (Number(day.income) / maxTrendValue) * 100 : 0
              const expenseHeight = maxTrendValue > 0 ? (Number(day.expense) / maxTrendValue) * 100 : 0
              return (
                <div key={index} className="flex-1 flex flex-col gap-0.5 group relative">
                  <div className="flex-1 relative">
                    {/* Income bar */}
                    <div
                      className="absolute bottom-0 w-full bg-green-400 rounded-t transition-all hover:bg-green-500"
                      style={{ height: `${incomeHeight}%` }}
                      title={`Income: ${formatCurrency(day.income)}`}
                    />
                    {/* Expense bar */}
                    <div
                      className="absolute bottom-0 w-full bg-red-400 rounded-t opacity-70 transition-all hover:opacity-100"
                      style={{ height: `${expenseHeight}%` }}
                      title={`Expense: ${formatCurrency(day.expense)}`}
                    />
                  </div>
                  <div className="text-xs text-gray-400 text-center truncate">
                    {new Date(day.date).getDate()}
                  </div>
                </div>
              )
            })}
          </div>

          <div className="flex items-center justify-center gap-6 mt-4">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-green-400 rounded"></div>
              <span className="text-sm text-gray-600">Income</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-red-400 rounded"></div>
              <span className="text-sm text-gray-600">Expense</span>
            </div>
          </div>
        </div>

        {/* Category Breakdown */}
        <div className="card p-6">
          <h2 className="text-lg font-semibold mb-4">Expense by Category</h2>
          {statistics?.categoryExpense && Object.keys(statistics.categoryExpense).length > 0 ? (
            <div className="space-y-3">
              {Object.entries(statistics.categoryExpense)
                .sort(([, a], [, b]) => b - a)
                .map(([category, amount]) => {
                  const percentage = statistics.categoryPercentages?.[category] || 0
                  const color = statistics.categoryColors?.[category] || '#6366f1'
                  return (
                    <div key={category}>
                      <div className="flex justify-between text-sm mb-1">
                        <span className="font-medium">{category}</span>
                        <span className="text-gray-500">{formatCurrency(amount)} ({percentage}%)</span>
                      </div>
                      <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                        <div
                          className="h-full rounded-full transition-all"
                          style={{ width: `${percentage}%`, backgroundColor: color }}
                        />
                      </div>
                    </div>
                  )
                })}
            </div>
          ) : (
            <p className="text-gray-500 text-center py-8">No expense data</p>
          )}
        </div>
      </div>

      {/* Recent Activity */}
      <div className="card">
        <div className="px-6 py-4 border-b flex items-center justify-between">
          <h2 className="text-lg font-semibold">Recent Activity</h2>
          <span className="text-sm text-gray-500">{recentActivity.length} transactions</span>
        </div>
        <div className="max-h-96 overflow-y-auto">
          {recentActivity.length > 0 ? (
            <div className="divide-y">
              {recentActivity.map((activity) => (
                <div key={activity.id} className="px-6 py-4 flex items-center gap-4 hover:bg-gray-50">
                  <div
                    className="w-10 h-10 rounded-full flex items-center justify-center text-lg"
                    style={{ backgroundColor: activity.categoryColor + '20' }}
                  >
                    {activity.categoryIcon || '📦'}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-gray-900 truncate">{activity.description}</p>
                    <p className="text-sm text-gray-500">
                      {activity.categoryName} • {formatTime(activity.createdAt)}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className={`font-semibold ${activity.type === 'INCOME' ? 'text-green-600' : 'text-red-600'}`}>
                      {activity.type === 'INCOME' ? '+' : '-'}{formatCurrency(activity.amount)}
                    </p>
                    <p className="text-xs text-gray-400">{activity.type}</p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-500 text-center py-12">No recent activity</p>
          )}
        </div>
      </div>

      {/* Statistics Summary */}
      {statistics && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="card p-6">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-blue-100 rounded-full">
                <PieChart className="text-blue-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">Average Transaction</p>
                <p className="text-xl font-bold">{formatCurrency(statistics.averageTransaction)}</p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-red-100 rounded-full">
                <TrendingDown className="text-red-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">Largest Expense</p>
                <p className="text-xl font-bold text-red-600">{formatCurrency(statistics.largestTransaction)}</p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-indigo-100 rounded-full">
                <BarChart3 className="text-indigo-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-500">Total Transactions</p>
                <p className="text-xl font-bold">{statistics.totalTransactions || 0}</p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
