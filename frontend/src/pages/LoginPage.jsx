import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider.jsx'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(form)
      navigate(location.state?.from || '/', { replace: true })
    } catch (requestError) {
      setError(requestError.response?.status === 401
        ? '이메일 또는 비밀번호를 확인해 주세요.'
        : '로그인 중 문제가 생겼습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-copy">
        <span className="eyebrow">다시 만났네요</span>
        <h1>내 물건의 다음 가치를 찾아보세요.</h1>
        <p>서로의 필요가 이어지면 2자부터 4자까지 새로운 교환 경로가 열립니다.</p>
      </div>
      <form className="panel auth-form" onSubmit={submit}>
        <h2>로그인</h2>
        {error && <p role="alert" className="error-message">{error}</p>}
        <label>이메일<input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required /></label>
        <label>비밀번호<input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required /></label>
        <button className="primary-button" disabled={submitting}>{submitting ? '로그인 중…' : '로그인'}</button>
        <p>아직 계정이 없나요? <Link to="/signup">회원가입</Link></p>
      </form>
    </section>
  )
}
