import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider.jsx'
import { apiErrorMessage } from '../api/client.js'

export default function SignupPage() {
  const { signup } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '', name: '', nickname: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const submit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await signup(form)
      navigate('/login', { replace: true })
    } catch (requestError) {
      setError(apiErrorMessage(requestError, '회원가입 정보를 확인해 주세요.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="narrow-page">
      <form className="panel auth-form" onSubmit={submit}>
        <span className="eyebrow">ValueSwap 시작하기</span>
        <h1>회원가입</h1>
        {error && <p role="alert" className="error-message">{error}</p>}
        <label>이름<input value={form.name} onChange={update('name')} required /></label>
        <label>닉네임<input value={form.nickname} onChange={update('nickname')} required /></label>
        <label>이메일<input type="email" value={form.email} onChange={update('email')} required /></label>
        <label>비밀번호<input type="password" minLength="8" value={form.password} onChange={update('password')} required /></label>
        <button className="primary-button" disabled={submitting}>{submitting ? '가입 중…' : '회원가입'}</button>
        <p>이미 계정이 있나요? <Link to="/login">로그인</Link></p>
      </form>
    </section>
  )
}
