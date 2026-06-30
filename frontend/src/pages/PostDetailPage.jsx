import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { postsApi } from '../api/posts.js'
import ItemCard from '../components/ItemCard.jsx'

export default function PostDetailPage() {
  const { id } = useParams()
  const [post, setPost] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { postsApi.detail(id).then(setPost).catch(() => setError('교환 글을 찾을 수 없습니다.')) }, [id])
  if (error) return <p role="alert" className="error-message content-section">{error}</p>
  if (!post) return <p className="status-message">교환 글을 불러오는 중입니다.</p>
  return (
    <article className="detail-page">
      <header className="detail-header">
        <span className="eyebrow">{post.region} · {post.status}</span>
        <h1>{post.title}</h1>
        <p>{post.description}</p>
        <div className="author-card"><strong>{post.author.nickname}</strong><span>신뢰도 {post.author.trustScore}</span><span>{post.author.role}</span></div>
      </header>
      <section><h2>내가 제공해요</h2><div className="item-grid">{post.provideItems.map((item) => <ItemCard key={item.id} item={item} kind="provide" />)}</div></section>
      <section><h2>이것을 원해요</h2><div className="item-grid">{post.wantItems.map((item) => <ItemCard key={item.id} item={item} kind="want" />)}</div></section>
    </article>
  )
}
