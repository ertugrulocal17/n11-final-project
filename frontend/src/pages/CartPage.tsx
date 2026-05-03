import { ProductImage } from '@/components/product/ProductImage'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { ApiError, apiFetch } from '@/lib/api'
import { formatTry } from '@/lib/format'
import type { Cart, OrderDetail } from '@/types/models'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

export function CartPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()

  const { data: cart, isLoading } = useQuery({
    queryKey: ['cart'],
    queryFn: () => apiFetch<Cart>('/api/v1/cart'),
  })

  const updateMutation = useMutation({
    mutationFn: async ({ productId, quantity }: { productId: number; quantity: number }) =>
      apiFetch<Cart>(`/api/v1/cart/items/${productId}`, {
        method: 'PATCH',
        body: JSON.stringify({ quantity }),
      }),
    onSuccess: () => void qc.invalidateQueries({ queryKey: ['cart'] }),
    onError: (e) => toast.error(e instanceof ApiError ? e.message : 'Güncellenemedi'),
  })

  const removeMutation = useMutation({
    mutationFn: async (productId: number) => {
      await apiFetch<void>(`/api/v1/cart/items/${productId}`, { method: 'DELETE' })
    },
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['cart'] })
      toast.success('Ürün kaldırıldı')
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.message : 'Kaldırılamadı'),
  })

  const orderMutation = useMutation({
    mutationFn: () => apiFetch<OrderDetail>('/api/v1/orders', { method: 'POST' }),
    onSuccess: (order) => {
      void qc.invalidateQueries({ queryKey: ['cart'] })
      void qc.invalidateQueries({ queryKey: ['orders'] })
      toast.success('Sipariş oluşturuldu')
      navigate(`/orders/${order.id}`)
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.message : 'Sipariş oluşturulamadı'),
  })

  if (isLoading || !cart) {
    return (
      <div className="mx-auto max-w-3xl space-y-4">
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-40 w-full" />
        <Skeleton className="h-40 w-full" />
      </div>
    )
  }

  if (cart.lines.length === 0) {
    return (
      <Card className="mx-auto max-w-lg text-center">
        <CardHeader>
          <CardTitle>Sepetiniz boş</CardTitle>
        </CardHeader>
        <CardContent>
          <Button asChild>
            <Link to="/">Alışverişe başla</Link>
          </Button>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="mx-auto grid max-w-4xl gap-8 lg:grid-cols-[1fr_320px]">
      <div className="space-y-4">
        <h1 className="font-display text-2xl font-bold">Sepet</h1>
        {cart.lines.map((line) => (
          <Card key={line.productId}>
            <CardContent className="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex min-w-0 flex-1 gap-4">
                <Link
                  to={`/products/${line.productId}`}
                  className="relative size-20 shrink-0 overflow-hidden rounded-lg border border-border/60 sm:size-24"
                >
                  <ProductImage
                    src={line.imageUrl}
                    alt={line.name}
                    className="size-full"
                    fallbackLetter={line.name}
                  />
                </Link>
                <div className="min-w-0 flex-1">
                <Link
                  to={`/products/${line.productId}`}
                  className="font-medium hover:text-primary"
                >
                  {line.name}
                </Link>
                <p className="text-xs text-muted-foreground">{line.sku}</p>
                <p className="mt-1 text-sm">{formatTry(line.unitPrice)} × {line.quantity}</p>
                {line.imageUrl ? (
                  <a
                    href={line.imageUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="mt-1 inline-block text-[11px] text-primary/80 underline-offset-2 hover:underline"
                    onClick={(e) => e.stopPropagation()}
                  >
                    Görsel bağlantısı
                  </a>
                ) : null}
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-2 sm:justify-end">
                <Input
                  type="number"
                  min={1}
                  max={999}
                  className="w-20"
                  defaultValue={line.quantity}
                  key={`${line.productId}-${line.quantity}`}
                  onBlur={(e) => {
                    const v = Number(e.target.value)
                    if (Number.isNaN(v) || v < 1) return
                    if (v !== line.quantity) updateMutation.mutate({ productId: line.productId, quantity: v })
                  }}
                />
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => removeMutation.mutate(line.productId)}
                  disabled={removeMutation.isPending}
                >
                  Kaldır
                </Button>
              </div>
              <div className="text-right font-display font-semibold text-primary sm:min-w-[100px]">
                {formatTry(line.lineTotal)}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card className="h-fit lg:sticky lg:top-24">
        <CardHeader>
          <CardTitle className="text-lg">Özet</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Ürün adedi</span>
            <Badge variant="secondary">{cart.totalQuantity}</Badge>
          </div>
          <Separator />
          <div className="flex justify-between font-display text-xl font-bold">
            <span>Ara toplam</span>
            <span className="text-primary">{formatTry(cart.subtotal)}</span>
          </div>
          <Button
            className="w-full"
            size="lg"
            disabled={orderMutation.isPending}
            onClick={() => orderMutation.mutate()}
          >
            {orderMutation.isPending ? 'İşleniyor…' : 'Siparişi tamamla'}
          </Button>
          <p className="text-center text-xs text-muted-foreground">
            Sipariş sonrası ödeme adımına yönlendirileceksiniz.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
