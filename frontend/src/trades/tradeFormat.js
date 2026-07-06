export function formatUnreadCount(count) {
  if (!count || count < 1) return ''
  return count > 99 ? '99+' : String(count)
}

export function formatMessageTime(value, now = new Date()) {
  const date = new Date(value)
  const period = date.getHours() < 12 ? '오전' : '오후'
  const hour = date.getHours() % 12 || 12
  const time = `${period} ${hour}:${String(date.getMinutes()).padStart(2, '0')}`
  const sameDay = date.getFullYear() === now.getFullYear()
    && date.getMonth() === now.getMonth() && date.getDate() === now.getDate()
  if (sameDay) return time
  const datePart = `${date.getMonth() + 1}월 ${date.getDate()}일`
  return date.getFullYear() === now.getFullYear() ? `${datePart} ${time}` : `${date.getFullYear()}년 ${datePart} ${time}`
}
