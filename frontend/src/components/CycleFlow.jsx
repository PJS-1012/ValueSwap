export default function CycleFlow({ edges = [] }) {
  const ordered = [...edges].sort((a, b) => a.orderIndex - b.orderIndex)
  return (
    <div className={`cycle-flow cycle-${ordered.length}`} aria-label={`${ordered.length}자 교환 흐름`}>
      {ordered.map((edge) => (
        <article className="cycle-edge" data-testid="cycle-edge" key={`${edge.orderIndex}-${edge.fromUserId || edge.fromNickname}`}>
          <div className="flow-people"><strong>{edge.fromNickname} → {edge.toNickname}</strong></div>
          <p><span>{edge.provideItemName}</span><b>{edge.score}점</b></p>
        </article>
      ))}
    </div>
  )
}
