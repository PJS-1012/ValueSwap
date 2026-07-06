import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { matchesApi } from '../api/matches.js'
import { useAuth } from '../auth/AuthProvider.jsx'
import CycleFlow from '../components/CycleFlow.jsx'
import { getMatchFitLabel } from '../matching/matchScore.js'
import { useTradeRooms } from '../trades/TradeRoomProvider.jsx'

const acceptLabels = { PENDING: '응답 대기', ACCEPTED: '수락 완료', REJECTED: '거절' }

export default function MatchDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const { rooms } = useTradeRooms()
  const [match, setMatch] = useState(null)
  const [error, setError] = useState('')
  const [updating, setUpdating] = useState(false)
  useEffect(() => { matchesApi.detail(id).then(setMatch).catch(() => setError('매칭 상세 정보를 불러오지 못했습니다.')) }, [id])
  const respond = async (action) => {
    setUpdating(true); setError('')
    try { setMatch(await matchesApi[action](id)) }
    catch (requestError) { setError(requestError.response?.data?.message || '참여 상태를 변경하지 못했습니다.') }
    finally { setUpdating(false) }
  }
  if (error && !match) return <p role="alert" className="error-message content-section">{error}</p>
  if (!match) return <p className="status-message">교환 경로를 불러오고 있어요.</p>
  const mine = match.participants.find((participant) => participant.userId === user?.id)
  const tradeRoom = rooms.find((room) => room.matchId === match.id)
  const open = !['ACCEPTED', 'REJECTED', 'EXPIRED', 'COMPLETED'].includes(match.status)
  return <section className="content-section match-detail">
    <header className="match-detail-header"><div><span className="eyebrow">{match.matchType}</span><h1>{match.score}점 · {getMatchFitLabel(match.score)}</h1><p>100점 만점 적합도입니다. 카테고리·이름·태그·가치·지역·신뢰도를 비교하며, 입력되지 않은 선택 항목은 평가 기준에서 제외합니다.</p></div><span className="status-pill">{match.status}</span></header>
    {error && <p role="alert" className="error-message">{error}</p>}
    <CycleFlow edges={match.edges} />
    <section className="participant-panel panel"><h2>참여자 확인 상태</h2>{match.participants.map((participant) => <div className="participant-row" key={participant.userId}><div><strong>{participant.nickname}</strong><span>{participant.postTitle}</span></div><b>{acceptLabels[participant.acceptStatus]}</b></div>)}</section>
    {mine?.acceptStatus === 'PENDING' && open && <div className="match-actions"><button className="primary-button" disabled={updating} onClick={() => respond('accept')}>참여 수락</button><button className="secondary-button" disabled={updating} onClick={() => respond('reject')}>거절</button></div>}
    {match.status === 'ACCEPTED' && <p className="success-message">전원이 수락해 거래가 진행 중입니다.</p>}
    {tradeRoom && <Link className="primary-button inline-action" to={`/trades/${tradeRoom.id}`}>거래방으로 이동</Link>}
  </section>
}
