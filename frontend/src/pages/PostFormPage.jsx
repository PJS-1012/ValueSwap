import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { postsApi } from '../api/posts.js'
import { apiErrorMessage } from '../api/client.js'

const categories = [
  ['FOOD', '음식'], ['FOOD_MATERIAL', '식재료'], ['DAILY_GOODS', '생활용품'], ['ELECTRONICS', '전자기기'],
  ['COUPON', '쿠폰'], ['SERVICE', '서비스'], ['TALENT', '재능'], ['DESIGN', '디자인'], ['BEAUTY', '뷰티'], ['LESSON', '레슨'], ['ETC', '기타'],
]
const newProvide = () => ({ category: 'FOOD', subCategory: '', name: '', description: '', quantity: 1, estimatedValue: '', tags: '' })
const newWant = () => ({ category: 'FOOD', subCategory: '', name: '', description: '', quantity: 1, minValue: '', maxValue: '', tags: '' })
const parseTags = (value) => [...new Set(value.split(',').map((tag) => tag.trim()).filter(Boolean))]

function CategorySelect({ id, label, value, onChange }) {
  return <label htmlFor={id}>{label}<select id={id} value={value} onChange={onChange}>{categories.map(([key, text]) => <option key={key} value={key}>{text}</option>)}</select></label>
}

export default function PostFormPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', description: '', region: '', provideItems: [newProvide()], wantItems: [newWant()] })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const updateRoot = (field) => (e) => setForm({ ...form, [field]: e.target.value })
  const updateItem = (kind, index, field, value) => setForm({ ...form, [kind]: form[kind].map((item, i) => i === index ? { ...item, [field]: value } : item) })
  const removeItem = (kind, index) => setForm({ ...form, [kind]: form[kind].filter((_, i) => i !== index) })

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    if (form.wantItems.some((item) => Number(item.minValue) > Number(item.maxValue))) {
      setError('최소 가치는 최대 가치보다 클 수 없습니다.')
      return
    }
    if (!form.title.trim() || !form.description.trim() || !form.region.trim()
      || form.provideItems.some((item) => !item.name.trim() || !item.subCategory.trim())
      || form.wantItems.some((item) => !item.name.trim() || !item.subCategory.trim())) {
      setError('제목, 설명, 지역과 항목 이름을 모두 입력해 주세요.')
      return
    }
    const payload = {
      title: form.title.trim(), description: form.description.trim(), region: form.region.trim(),
      provideItems: form.provideItems.map((item) => ({ ...item, quantity: Number(item.quantity), estimatedValue: Number(item.estimatedValue), tags: parseTags(item.tags) })),
      wantItems: form.wantItems.map((item) => ({ ...item, quantity: Number(item.quantity), minValue: Number(item.minValue), maxValue: Number(item.maxValue), tags: parseTags(item.tags) })),
    }
    setSubmitting(true)
    try {
      const created = await postsApi.create(payload)
      navigate(`/posts/${created.id}`)
    } catch (requestError) {
      setError(apiErrorMessage(requestError, '교환 글을 등록하지 못했습니다.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="form-page">
      <header><span className="eyebrow">새 교환 경로 만들기</span><h1>교환 글 등록</h1><p>제공할 것과 원하는 것을 구체적으로 적을수록 더 정확하게 연결됩니다.</p></header>
      <form onSubmit={submit} noValidate>
        {error && <p role="alert" className="error-message">{error}</p>}
        <section className="panel form-section"><h2>기본 정보</h2><div className="field-grid">
          <label className="wide">제목<input value={form.title} onChange={updateRoot('title')} /></label>
          <label>거래 지역<input value={form.region} onChange={updateRoot('region')} /></label>
          <label className="wide">설명<textarea rows="4" value={form.description} onChange={updateRoot('description')} /></label>
        </div></section>
        <section className="panel form-section"><div className="form-section-title"><div><span className="eyebrow provide-text">PROVIDE</span><h2>내가 제공해요</h2></div><button type="button" className="secondary-button" onClick={() => setForm({ ...form, provideItems: [...form.provideItems, newProvide()] })}>제공 항목 추가</button></div>
          {form.provideItems.map((item, index) => <div className="item-form" data-testid="provide-item" key={`provide-${index}`}>
            <div className="item-form-header"><h3>제공 항목 {index + 1}</h3><button type="button" className="danger-link" disabled={form.provideItems.length === 1} onClick={() => removeItem('provideItems', index)}>제공 항목 삭제</button></div>
            <div className="field-grid"><CategorySelect id={`provide-category-${index}`} label="제공 카테고리" value={item.category} onChange={(e) => updateItem('provideItems', index, 'category', e.target.value)} />
              <label>제공 세부 카테고리<input value={item.subCategory} onChange={(e) => updateItem('provideItems', index, 'subCategory', e.target.value)} /></label>
              <label className="wide">제공 이름<input value={item.name} onChange={(e) => updateItem('provideItems', index, 'name', e.target.value)} /></label>
              <label>제공 수량<input type="number" min="1" value={item.quantity} onChange={(e) => updateItem('provideItems', index, 'quantity', e.target.value)} /></label>
              <label>제공 가치<input type="number" min="0" value={item.estimatedValue} onChange={(e) => updateItem('provideItems', index, 'estimatedValue', e.target.value)} /></label>
              <label className="wide">제공 태그<input value={item.tags} onChange={(e) => updateItem('provideItems', index, 'tags', e.target.value)} placeholder="쉼표로 구분: 점심, 마포" /></label>
            </div></div>)}
        </section>
        <section className="panel form-section"><div className="form-section-title"><div><span className="eyebrow want-text">WANT</span><h2>이것을 원해요</h2></div><button type="button" className="secondary-button" onClick={() => setForm({ ...form, wantItems: [...form.wantItems, newWant()] })}>희망 항목 추가</button></div>
          {form.wantItems.map((item, index) => <div className="item-form" data-testid="want-item" key={`want-${index}`}>
            <div className="item-form-header"><h3>희망 항목 {index + 1}</h3><button type="button" className="danger-link" disabled={form.wantItems.length === 1} onClick={() => removeItem('wantItems', index)}>희망 항목 삭제</button></div>
            <div className="field-grid"><CategorySelect id={`want-category-${index}`} label="희망 카테고리" value={item.category} onChange={(e) => updateItem('wantItems', index, 'category', e.target.value)} />
              <label>희망 세부 카테고리<input value={item.subCategory} onChange={(e) => updateItem('wantItems', index, 'subCategory', e.target.value)} /></label>
              <label className="wide">희망 이름<input value={item.name} onChange={(e) => updateItem('wantItems', index, 'name', e.target.value)} /></label>
              <label>희망 수량<input type="number" min="1" value={item.quantity} onChange={(e) => updateItem('wantItems', index, 'quantity', e.target.value)} /></label>
              <label>희망 최소 가치<input type="number" min="0" value={item.minValue} onChange={(e) => updateItem('wantItems', index, 'minValue', e.target.value)} /></label>
              <label>희망 최대 가치<input type="number" min="0" value={item.maxValue} onChange={(e) => updateItem('wantItems', index, 'maxValue', e.target.value)} /></label>
              <label className="wide">희망 태그<input value={item.tags} onChange={(e) => updateItem('wantItems', index, 'tags', e.target.value)} placeholder="쉼표로 구분" /></label>
            </div></div>)}
        </section>
        <div className="form-actions"><button className="primary-button" disabled={submitting}>{submitting ? '등록 중…' : '교환 글 등록'}</button></div>
      </form>
    </section>
  )
}
