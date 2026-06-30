import { Link } from 'react-router-dom'

const statusLabels = { ACTIVE: '교환 가능', IN_EXCHANGE: '교환 진행 중', COMPLETED: '교환 완료', CANCELLED: '취소됨' }

export default function PostCard({ post }) {
  return (
    <article className="post-card">
      <div className="card-meta">
        <span className={`status ${post.status?.toLowerCase()}`}>{statusLabels[post.status] || post.status}</span>
        <span>{post.region}</span>
      </div>
      <h3><Link to={`/posts/${post.id}`}>{post.title}</Link></h3>
      {post.description && <p>{post.description}</p>}
      {(post.provideItems || post.wantItems) ? (
        <div className="swap-summary">
          <span>제공 {post.provideItems?.length ?? 0}</span>
          <span aria-hidden="true">→</span>
          <span>희망 {post.wantItems?.length ?? 0}</span>
        </div>
      ) : <time className="post-date">{new Date(post.createdAt).toLocaleDateString('ko-KR')} 등록</time>}
      <div className="author-line">
        <span>{post.author?.nickname || post.authorNickname}</span>
        <span>신뢰도 {post.author?.trustScore ?? post.authorTrustScore ?? '-'}</span>
      </div>
    </article>
  )
}
