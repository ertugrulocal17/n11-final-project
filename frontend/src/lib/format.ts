export function formatTry(amount: string | number): string {
  const n = typeof amount === 'string' ? Number(amount) : amount
  if (Number.isNaN(n)) return String(amount)
  return new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY' }).format(n)
}
