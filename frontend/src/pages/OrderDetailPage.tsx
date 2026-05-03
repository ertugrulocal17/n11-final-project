import { ProductImage } from '@/components/product/ProductImage'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { ApiError, apiFetch } from '@/lib/api'
import { formatTry } from '@/lib/format'
import type { OrderDetail, OrderStatus, PaymentInitialize } from '@/types/models'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { toast } from 'sonner'

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

export function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const orderId = Number(id)
  const { data: order, isLoading, isError, error, refetch } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => apiFetch<OrderDetail>(`/api/v1/orders/${orderId}`),
    enabled: Number.isFinite(orderId),
  })

  const payMutation = useMutation({
    mutationFn: () =>
      apiFetch<PaymentInitialize>(`/api/v1/orders/${orderId}/payment/iyzico/initialize`, {
        method: 'POST',
      }),
    onSuccess: (res) => {
      if (res.paymentPageUrl) {
        window.location.href = res.paymentPageUrl
        return
      }
      toast.error('Ödeme URL alınamadı')
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.message : 'Ödeme başlatılamadı'),
  })

  if (!Number.isFinite(orderId)) {
    return <p className="text-center text-muted-foreground">Geçersiz sipariş</p>
  }

  if (isLoading) {
    return (
      <div className="mx-auto max-w-3xl space-y-4">
        <Skeleton className="h-10 w-64" />
        <Skeleton className="h-48 w-full" />
      </div>
    )
  }

  if (isError || !order) {
    return (
      <Card className="mx-auto max-w-lg p-8 text-center">
        <p className="text-destructive">{(error as Error)?.message ?? 'Sipariş bulunamadı'}</p>
        <Button asChild className="mt-4" variant="outline">
          <Link to="/orders">Siparişlere dön</Link>
        </Button>
      </Card>
    )
  }

  return (
    <div className="mx-auto max-w-3xl space-y-8">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <Button variant="ghost" size="sm" asChild className="mb-2 -ml-2">
            <Link to="/orders">← Siparişler</Link>
          </Button>
          <h1 className="font-display text-2xl font-bold">Sipariş #{order.id}</h1>
          <p className="text-sm text-muted-foreground">
            {new Date(order.createdAt).toLocaleString('tr-TR', { dateStyle: 'full', timeStyle: 'short' })}
          </p>
        </div>
        <div className="flex flex-col items-end gap-2">
          <Badge variant={statusVariant(order.status)}>{statusLabel(order.status)}</Badge>
          {order.status === 'CREATED' ? (
            <Button disabled={payMutation.isPending} onClick={() => payMutation.mutate()}>
              {payMutation.isPending ? 'Yönlendiriliyor…' : 'İyzico ile öde'}
            </Button>
          ) : null}
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Kalemler</CardTitle>
        </CardHeader>
        <CardContent className="space-y-0 divide-y divide-border">
          {order.lines.map((l) => (
            <div
              key={`${l.productId}-${l.productSku}`}
              className="flex flex-wrap items-center justify-between gap-4 py-4 first:pt-0"
            >
              <div className="flex min-w-0 flex-1 items-start gap-4">
                <Link
                  to={`/products/${l.productId}`}
                  className="relative size-16 shrink-0 overflow-hidden rounded-lg border border-border/60 sm:size-20"
                >
                  <ProductImage
                    src={l.imageUrl}
                    alt={l.productName}
                    className="size-full"
                    fallbackLetter={l.productName}
                  />
                </Link>
                <div className="min-w-0">
                  <p className="font-medium">{l.productName}</p>
                  <p className="text-xs text-muted-foreground">{l.productSku}</p>
                  <p className="text-sm text-muted-foreground">
                    {formatTry(l.unitPrice)} × {l.quantity}
                  </p>
                  {l.imageUrl ? (
                    <a
                      href={l.imageUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="mt-1 inline-block text-[11px] text-primary/80 underline-offset-2 hover:underline"
                    >
                      Görsel bağlantısı
                    </a>
                  ) : null}
                </div>
              </div>
              <p className="font-display font-semibold text-primary">{formatTry(l.lineTotal)}</p>
            </div>
          ))}
          <Separator className="my-4" />
          <div className="flex justify-between pt-2 font-display text-xl font-bold">
            <span>Toplam</span>
            <span className="text-primary">{formatTry(order.totalAmount)}</span>
          </div>
        </CardContent>
      </Card>

      {order.status === 'PAID' ? (
        <p className="text-center text-sm text-muted-foreground">
          Ödeme tamamlandı. İyzico dönüşünde sipariş durumu güncellenir.
        </p>
      ) : null}

      <Button variant="outline" onClick={() => void refetch()}>
        Durumu yenile
      </Button>
    </div>
  )
}
