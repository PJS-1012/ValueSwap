export function getMatchFitLabel(score) {
  if (score >= 85) return '매우 높은 적합도'
  if (score >= 70) return '높은 적합도'
  if (score >= 55) return '보통 적합도'
  if (score >= 40) return '낮은 적합도'
  return '매우 낮은 적합도'
}
