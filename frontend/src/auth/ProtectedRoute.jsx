import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthProvider.jsx'

export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <p className="status-message">로그인 상태를 확인하고 있어요.</p>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return children
}
