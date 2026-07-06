import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { postsApi } from '../api/posts.js'
import { apiErrorMessage } from '../api/client.js'
import { regions } from '../data/regions.js'

const categories = [
  ['FOOD', '음식'], ['FOOD_MATERIAL', '식재료'], ['DAILY_GOODS', '생활용품'], ['ELECTRONICS', '전자기기'],
  ['COUPON', '쿠폰'], ['SERVICE', '서비스'], ['TALENT', '재능'], ['DESIGN', '디자인'], ['BEAUTY', '뷰티'], ['LESSON', '레슨'], ['ETC', '기타'],
]
const valuePolicies = [['DIRECT', '금액 직접 입력'], ['NEGOTIABLE', '협의 가능'], ['OFFER_REQUESTED', '상대 제안 받기']]
const newProvide = () => ({ category: 'FOOD', subCategory: '', name: '', description: '', quantity: 1, valuePolicy: 'DIRECT', estimatedValue: '', tags: '' })
const newWant = () => ({ category: 'FOOD', subCategory: '', name: '', description: '', quantity: 1, valuePolicy: 'DIRECT', minValue: '', maxValue: '', tags: '' })
const parseTags = (value) => [...new Set(value.split(',').map((tag) => tag.trim()).filter(Boolean))]

function Field({ label, error, children, className = '' }) {
  return <label className={className}>{label}{children}{error && <span className="field-error">{error}</span>}</label>
}

function CategorySelect({ id, label, value, onChange }) {
  return <label htmlFor={id}>{label}<select id={id} value={value} onChange={onChange}>{categories.map(([key, text]) => <option key={key} value={key}>{text}</option>)}</select></label>
}

function ValuePolicySelect({ id, label, value, onChange }) {
  return <label htmlFor={id}>{label}<select id={id} value={value} onChange={onChange}>{valuePolicies.map(([key, text]) => <option key={key} value={key}>{text}</option>)}</select></label>
}

export default function PostFormPage() {
  const navigate = useNavigate()
  const formRef = useRef(null)
  const [form, setForm] = useState({ title: '', description: '', region: '', provideItems: [newProvide()], wantItems: [newWant()] })
  const [errors, setErrors] = useState({})
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const updateRoot = (field) => (e) => { setForm({ ...form, [field]: e.target.value }); setErrors({ ...errors, [field]: null }) }
  const updateItem = (kind, index, field, value) => setForm({ ...form, [kind]: form[kind].map((item, i) => i === index ? { ...item, [field]: value } : item) })
  const removeItem = (kind, index) => setForm({ ...form, [kind]: form[kind].filter((_, i) => i !== index) })

  const validate = () => {
    const next = {}
    if (!form.title.trim()) next.title = '제목을 입력해 주세요.'
    if (!form.description.trim()) next.description = '설명을 입력해 주세요.'
    if (!form.region) next.region = '거래 지역을 선택해 주세요.'
    form.provideItems.forEach((item, index) => {
      if (!item.name.trim()) next[`provide-name-${index}`] = '제공 물품·서비스를 입력해 주세요.'
      if (item.valuePolicy === 'DIRECT' && item.estimatedValue === '') next[`provide-value-${index}`] = '가치를 입력하거나 다른 방식을 선택해 주세요.'
    })
    form.wantItems.forEach((item, index) => {
      if (!item.name.trim()) next[`want-name-${index}`] = '희망 물품·서비스를 입력해 주세요.'
      if (item.valuePolicy === 'DIRECT' && (item.minValue === '' || item.maxValue === '')) next[`want-value-${index}`] = '가치 범위를 입력하거나 다른 방식을 선택해 주세요.'
      if (item.valuePolicy === 'DIRECT' && Number(item.minValue) > Number(item.maxValue)) next[`want-value-${index}`] = '최소 가치는 최대 가치보다 클 수 없습니다.'
    })
    return next
  }

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    const nextErrors = validate()
    if (Object.keys(nextErrors).length) {
      setErrors(nextErrors)
      setError(Object.values(nextErrors).find((message) => message.includes('최소 가치')) || Object.values(nextErrors)[0])
      requestAnimationFrame(() => formRef.current?.querySelector('[aria-invalid="true"]')?.focus())
      return
    }
    const payload = {
      title: form.title.trim(), description: form.description.trim(), region: form.region,
      provideItems: form.provideItems.map((item) => ({ ...item, subCategory: item.subCategory.trim() || null, quantity: Number(item.quantity), estimatedValue: item.valuePolicy === 'DIRECT' ? Number(item.estimatedValue) : null, tags: parseTags(item.tags) })),
      wantItems: form.wantItems.map((item) => ({ ...item, subCategory: item.subCategory.trim() || null, quantity: Number(item.quantity), minValue: item.valuePolicy === 'DIRECT' ? Number(item.minValue) : null, maxValue: item.valuePolicy === 'DIRECT' ? Number(item.maxValue) : null, tags: parseTags(item.tags) })),
    }
    setSubmitting(true)
    try { const created = await postsApi.create(payload); navigate(`/posts/${created.id}`) }
    catch (requestError) { setError(apiErrorMessage(requestError, '교환 글을 등록하지 못했습니다.')) }
    finally { setSubmitting(false) }
  }

  return <section className="form-page">
    <header><span className="eyebrow">새 교환 경로 만들기</span><h1>교환 글 등록</h1><p>제공할 것과 원하는 것을 구체적으로 적을수록 더 정확하게 연결됩니다.</p></header>
    <form ref={formRef} onSubmit={submit} noValidate>
      {error && <p role="alert" className="error-message">{error}</p>}
      <section className="panel form-section"><h2>기본 정보</h2><div className="field-grid">
        <Field label="제목" error={errors.title} className="wide"><input value={form.title} aria-invalid={Boolean(errors.title)} onChange={updateRoot('title')} /></Field>
        <Field label="거래 지역" error={errors.region}><select value={form.region} aria-invalid={Boolean(errors.region)} onChange={updateRoot('region')}><option value="">지역 선택</option>{regions.map((region) => <option key={region}>{region}</option>)}</select></Field>
        <Field label="설명" error={errors.description} className="wide"><textarea rows="4" value={form.description} aria-invalid={Boolean(errors.description)} onChange={updateRoot('description')} /></Field>
      </div></section>
      <section className="panel form-section"><div className="form-section-title"><div><span className="eyebrow provide-text">PROVIDE</span><h2>내가 제공해요</h2></div><button type="button" className="secondary-button" onClick={() => setForm({ ...form, provideItems: [...form.provideItems, newProvide()] })}>제공 항목 추가</button></div>
        {form.provideItems.map((item, index) => <div className="item-form" data-testid="provide-item" key={`provide-${index}`}><div className="item-form-header"><h3>제공 항목 {index + 1}</h3><button type="button" className="danger-link" disabled={form.provideItems.length === 1} onClick={() => removeItem('provideItems', index)}>제공 항목 삭제</button></div><div className="field-grid">
          <CategorySelect id={`provide-category-${index}`} label="제공 카테고리" value={item.category} onChange={(e) => updateItem('provideItems', index, 'category', e.target.value)} />
          <label>제공 세부 카테고리 (선택)<input value={item.subCategory} onChange={(e) => updateItem('provideItems', index, 'subCategory', e.target.value)} /></label>
          <Field label="제공 물품·서비스" error={errors[`provide-name-${index}`]} className="wide"><input value={item.name} aria-invalid={Boolean(errors[`provide-name-${index}`])} onChange={(e) => updateItem('provideItems', index, 'name', e.target.value)} /></Field>
          <label>제공 수량<input type="number" min="1" value={item.quantity} onChange={(e) => updateItem('provideItems', index, 'quantity', e.target.value)} /></label>
          <ValuePolicySelect id={`provide-policy-${index}`} label="제공 가치 방식" value={item.valuePolicy} onChange={(e) => updateItem('provideItems', index, 'valuePolicy', e.target.value)} />
          {item.valuePolicy === 'DIRECT' && <Field label="제공 가치" error={errors[`provide-value-${index}`]}><input type="number" min="0" value={item.estimatedValue} aria-invalid={Boolean(errors[`provide-value-${index}`])} onChange={(e) => updateItem('provideItems', index, 'estimatedValue', e.target.value)} /></Field>}
          <label className="wide">제공 태그<input value={item.tags} onChange={(e) => updateItem('provideItems', index, 'tags', e.target.value)} placeholder="쉼표로 구분: 점심, 마포" /></label>
        </div></div>)}
      </section>
      <section className="panel form-section"><div className="form-section-title"><div><span className="eyebrow want-text">WANT</span><h2>이것을 원해요</h2></div><button type="button" className="secondary-button" onClick={() => setForm({ ...form, wantItems: [...form.wantItems, newWant()] })}>희망 항목 추가</button></div>
        {form.wantItems.map((item, index) => <div className="item-form" data-testid="want-item" key={`want-${index}`}><div className="item-form-header"><h3>희망 항목 {index + 1}</h3><button type="button" className="danger-link" disabled={form.wantItems.length === 1} onClick={() => removeItem('wantItems', index)}>희망 항목 삭제</button></div><div className="field-grid">
          <CategorySelect id={`want-category-${index}`} label="희망 카테고리" value={item.category} onChange={(e) => updateItem('wantItems', index, 'category', e.target.value)} />
          <label>희망 세부 카테고리 (선택)<input value={item.subCategory} onChange={(e) => updateItem('wantItems', index, 'subCategory', e.target.value)} /></label>
          <Field label="희망 물품·서비스" error={errors[`want-name-${index}`]} className="wide"><input value={item.name} aria-invalid={Boolean(errors[`want-name-${index}`])} onChange={(e) => updateItem('wantItems', index, 'name', e.target.value)} /></Field>
          <label>희망 수량<input type="number" min="1" value={item.quantity} onChange={(e) => updateItem('wantItems', index, 'quantity', e.target.value)} /></label>
          <ValuePolicySelect id={`want-policy-${index}`} label="희망 가치 방식" value={item.valuePolicy} onChange={(e) => updateItem('wantItems', index, 'valuePolicy', e.target.value)} />
          {item.valuePolicy === 'DIRECT' && <><Field label="희망 최소 가치" error={errors[`want-value-${index}`]}><input type="number" min="0" value={item.minValue} aria-invalid={Boolean(errors[`want-value-${index}`])} onChange={(e) => updateItem('wantItems', index, 'minValue', e.target.value)} /></Field><Field label="희망 최대 가치" error={errors[`want-value-${index}`]}><input type="number" min="0" value={item.maxValue} aria-invalid={Boolean(errors[`want-value-${index}`])} onChange={(e) => updateItem('wantItems', index, 'maxValue', e.target.value)} /></Field></>}
          <label className="wide">희망 태그<input value={item.tags} onChange={(e) => updateItem('wantItems', index, 'tags', e.target.value)} placeholder="쉼표로 구분" /></label>
        </div></div>)}
      </section>
      <div className="form-actions"><button className="primary-button" disabled={submitting}>{submitting ? '등록 중…' : '교환 글 등록'}</button></div>
    </form>
  </section>
}
