import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { postsApi } from '../api/posts.js'
import PostCard from '../components/PostCard.jsx'

export default function HomePage() {
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    postsApi.list().then(setPosts).catch(() => setError('교환 글을 불러오지 못했습니다.')).finally(() => setLoading(false))
  }, [])

  return (
    <>
      <section className="hero">
        <div>
          <span className="eyebrow">필요와 가치가 만나는 곳</span>
          <h1>둘이 안 맞으면,<br />셋이 바꾸면 됩니다.</h1>
          <p>ValueSwap은 최대 4명의 필요를 연결해 숨어 있던 교환 경로를 찾아줍니다.</p>
          <Link className="primary-button inline" to="/posts/new">내 교환 글 등록하기</Link>
        </div>
        <div className="hero-visual" aria-label="세 사람의 순환 교환 예시">
          <span>식사권</span><b>→</b><span>농산물</span><b>→</b><span>디자인</span><b>↗</b>
        </div>
      </section>
      <section className="content-section">
        <div className="section-heading"><div><span className="eyebrow">새로운 기회</span><h2>최근 교환 글</h2></div></div>
        {loading && <p className="status-message">교환 글을 불러오는 중입니다.</p>}
        {error && <p role="alert" className="error-message">{error}</p>}
        {!loading && !error && posts.length === 0 && <div className="empty-state"><h3>아직 등록된 교환 글이 없습니다.</h3><p>첫 번째 교환 경로를 열어 보세요.</p></div>}
        <div className="post-grid">{posts.map((post) => <PostCard key={post.id} post={post} />)}</div>
      </section>
    </>
  )
}
