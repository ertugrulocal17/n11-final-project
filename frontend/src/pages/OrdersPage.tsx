import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { apiFetch } from '@/lib/api'
import { formatTry } from '@/lib/format'
import type { OrderStatus, OrderSummary, PageResponse } from '@/types/models'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

function statusVariant(s: OrderStatus): 'default' | 'success' | 'warning' | 'muted' {
  if (s === 'PAID') return 'success'
  if (s === 'CREATED') return 'warning'
  return 'muted'
}

function statusLabel(s: OrderStatus): string {
  if (s === 'PAID') return 'Ödendi'
  if (s === 'CREATED') return 'Ödeme bekliyor'
  return 'İptal'
}

export function OrdersPage() {
  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ['orders', 0],
    queryFn: () => apiFetch<PageResponse<OrderSummary>>('/api/v1/orders?page=0&size=50&sort=createdAt,desc'),
  })

  if (isLoading) {
    return (
      <div className="mx-auto max-w-3xl space-y-3">
        <Skeleton className="h-10 w-40" />
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} className="h-24 w-full" />
        ))}
      </div>
    )
  }

  if (isError) {
    return (
      <div className="rounded-xl border border-destructive/30 bg-destructive/5 p-6 text-center text-sm text-destructive">
        {(error as Error).message}
        <Button variant="outline" className="mt-4" onClick={() => void refetch()}>
          Yeniden dene
        </Button>
      </div>
    )
  }

  const list = data?.content ?? []

  if (list.length === 0) {
    return (
      <Card className="mx-auto max-w-lg text-center">
        <CardContent className="py-12">
          <p className="text-muted-foreground">Henüz sipariş yok.</p>
          <Button asChild className="mt-4">
            <Link to="/">Alışverişe başla</Link>
          </Button>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <h1 className="font-display text-2xl font-bold">Siparişlerim</h1>
      <ul className="space-y-3">
        {list.map((o) => (
          <li key={o.id}>
            <Link to={`/orders/${o.id}`}>
              <Card className="transition-shadow hover:shadow-md">
                <CardContent className="flex flex-wrap items-center justify-between gap-3 p-4">
                  <div>
                    <p className="font-mono text-xs text-muted-foreground">#{o.id}</p>
                    <p className="font-medium">
                      {new Date(o.createdAt).toLocaleString('tr-TR', {
                        dateStyle: 'medium',
                        timeStyle: 'short',
                      })}
                    </p>
                  </div>
                  <Badge variant={statusVariant(o.status)}>{statusLabel(o.status)}</Badge>
                  <p className="font-display text-lg font-bold text-primary">{formatTry(o.totalAmount)}</p>
                </CardContent>
              </Card>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
