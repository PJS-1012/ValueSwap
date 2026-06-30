import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { matchesApi } from '../api/matches.js'
import CycleFlow from '../components/CycleFlow.jsx'

export default function MatchDetailPage() {
  const { id } = useParams()
  const [match, setMatch] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { matchesApi.detail(id).then(setMatch).catch(() => setError('매칭 상세 정보를 불러오지 못했습니다.')) }, [id])
  if (error) return <p role="alert" className="error-message content-section">{error}</p>
  if (!match) return <p className="status-message">교환 경로를 불러오고 있어요.</p>
  return (
    <section className="content-section match-detail">
      <header className="match-detail-header"><div><span className="eyebrow">{match.matchType}</span><h1>{match.score}점의 교환 경로</h1><p>각 화살표는 한 사용자의 희망이 어느 사용자의 제공 항목과 연결됐는지 나타냅니다.</p></div><span className="status-pill">{match.status}</span></header>
      <CycleFlow edges={match.edges} />
      <section className="participant-panel panel"><h2>참여자 확인 상태</h2>{match.participants.map((participant) => <div className="participant-row" key={participant.userId}><div><strong>{participant.nickname}</strong><span>{participant.postTitle}</span></div><b>{participant.acceptStatus}</b></div>)}</section>
    </section>
  )
}
