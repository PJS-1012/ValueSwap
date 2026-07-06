import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { matchesApi } from '../api/matches.js'
import CycleFlow from '../components/CycleFlow.jsx'
import { getMatchFitLabel } from '../matching/matchScore.js'

const typeLabels = { ONE_TO_ONE: '1:1 교환', THREE_PARTY: '3자 교환', FOUR_PARTY: '4자 교환' }

export default function MatchesPage() {
  const [matches, setMatches] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  useEffect(() => { matchesApi.mine().then(setMatches).catch(() => setError('매칭 후보를 불러오지 못했습니다.')).finally(() => setLoading(false)) }, [])
  const ordered = useMemo(() => [...matches].sort((a, b) => b.score - a.score || new Date(b.createdAt) - new Date(a.createdAt)), [matches])

  return (
    <section className="content-section matches-page">
      <header className="page-heading"><div><span className="eyebrow">MATCH CANDIDATES</span><h1>내 매칭 후보</h1></div><p>카테고리·이름·태그·가치·지역·신뢰도를 평가해 100점 만점으로 환산한 적합도입니다.</p></header>
      <details className="score-guide panel"><summary>적합도 기준 보기</summary><ul>
        <li>85~100점 · 매우 높은 적합도</li><li>70~84점 · 높은 적합도</li>
        <li>55~69점 · 보통 적합도</li><li>40~54점 · 낮은 적합도</li>
        <li>0~39점 · 매우 낮은 적합도</li>
      </ul></details>
      {loading && <p className="status-message">매칭 후보를 계산하고 있어요.</p>}
      {error && <p role="alert" className="error-message">{error}</p>}
      {!loading && !error && ordered.length === 0 && <div className="empty-state"><h2>아직 연결된 교환 경로가 없습니다.</h2><p>교환 글을 구체적으로 작성하면 더 정확한 후보를 찾을 수 있어요.</p></div>}
      <div className="match-list">{ordered.map((match) => (
        <article className="match-card" data-testid="match-card" key={match.id}>
          <header><div><span className="eyebrow">{typeLabels[match.matchType] || match.matchType}</span><div className="participant-list">{match.participants.map((participant) => <span key={participant.userId}>{participant.nickname}</span>)}</div></div><div className="score-summary"><strong className="score-badge" aria-label="매칭 점수">{match.score}점</strong><span>{getMatchFitLabel(match.score)}</span></div></header>
          <CycleFlow edges={match.edges} />
          <footer><span>{new Date(match.createdAt).toLocaleDateString('ko-KR')} 발견</span><Link to={`/matches/${match.id}`}>자세히 보기 →</Link></footer>
        </article>
      ))}</div>
    </section>
  )
}
