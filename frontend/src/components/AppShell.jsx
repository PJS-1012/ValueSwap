import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider.jsx'

export default function AppShell({ children }) {
  const { user, logout } = useAuth()
  return (
    <div className="app">
      <header className="site-header">
        <Link className="brand" to="/" aria-label="ValueSwap 홈">
          <span className="brand-mark">V</span>
          <span>ValueSwap</span>
        </Link>
        <nav aria-label="주요 메뉴">
          <NavLink to="/">교환 글</NavLink>
          {user && <NavLink to="/matches">매칭</NavLink>}
          {user && <NavLink to="/notifications">알림</NavLink>}
          {user && <NavLink to="/posts/new">글 등록</NavLink>}
          {user ? (
            <>
              <span className="user-chip">{user.nickname}</span>
              <button className="link-button" type="button" onClick={logout}>로그아웃</button>
            </>
          ) : (
            <>
              <NavLink to="/login">로그인</NavLink>
              <NavLink to="/signup">회원가입</NavLink>
            </>
          )}
        </nav>
      </header>
      <main>{children}</main>
      <footer>필요한 것과 가진 것을 연결하는 다자간 교환</footer>
    </div>
  )
}
