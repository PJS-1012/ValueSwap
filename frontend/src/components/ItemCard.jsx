const categoryLabels = {
  FOOD: '음식', FOOD_MATERIAL: '식재료', DAILY_GOODS: '생활용품', ELECTRONICS: '전자기기',
  COUPON: '쿠폰', SERVICE: '서비스', TALENT: '재능', DESIGN: '디자인', BEAUTY: '뷰티',
  LESSON: '레슨', ETC: '기타',
}

export default function ItemCard({ item, kind }) {
  const value = kind === 'provide'
    ? `${Number(item.estimatedValue).toLocaleString()}원 상당`
    : `${Number(item.minValue).toLocaleString()}~${Number(item.maxValue).toLocaleString()}원`
  return (
    <article className={`item-card ${kind}`}>
      <span className="eyebrow">{categoryLabels[item.category] || item.category} · {item.subCategory}</span>
      <h4>{item.name}</h4>
      {item.description && <p>{item.description}</p>}
      <strong>{item.quantity}개 · {value}</strong>
      {item.tags?.length > 0 && <div className="tags">{item.tags.map((tag) => <span key={tag}>#{tag}</span>)}</div>}
    </article>
  )
}
