import { Route, Routes } from 'react-router-dom'
import AppShell from './components/AppShell.jsx'
import ProtectedRoute from './auth/ProtectedRoute.jsx'
import HomePage from './pages/HomePage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import SignupPage from './pages/SignupPage.jsx'
import PostFormPage from './pages/PostFormPage.jsx'
import PostDetailPage from './pages/PostDetailPage.jsx'
import MatchesPage from './pages/MatchesPage.jsx'
import MatchDetailPage from './pages/MatchDetailPage.jsx'
import NotificationsPage from './pages/NotificationsPage.jsx'
import { NotificationProvider } from './notifications/NotificationProvider.jsx'

export default function App() {
  return (
    <NotificationProvider>
    <AppShell>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/posts/new" element={<ProtectedRoute><PostFormPage /></ProtectedRoute>} />
        <Route path="/posts/:id" element={<PostDetailPage />} />
        <Route path="/matches" element={<ProtectedRoute><MatchesPage /></ProtectedRoute>} />
        <Route path="/matches/:id" element={<ProtectedRoute><MatchDetailPage /></ProtectedRoute>} />
        <Route path="/notifications" element={<ProtectedRoute><NotificationsPage /></ProtectedRoute>} />
        <Route path="*" element={<section className="empty-state"><h1>페이지를 찾을 수 없습니다.</h1></section>} />
      </Routes>
    </AppShell>
    </NotificationProvider>
  )
}
