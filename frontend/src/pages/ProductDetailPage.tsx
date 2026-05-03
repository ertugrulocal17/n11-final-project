import { useAuth } from '@/auth/auth-context'
import { ProductImage } from '@/components/product/ProductImage'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { ApiError, apiFetch } from '@/lib/api'
import { formatTry } from '@/lib/format'
import type { Cart, Product } from '@/types/models'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Minus, Plus, ShoppingCart } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useState } from 'react'
import { toast } from 'sonner'

export function ProductDetailPage() {
  const { id } = useParams<{ id: string }>()
  const productId = Number(id)
  const { token } = useAuth()
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [qty, setQty] = useState(1)

  const { data: product, isLoading, isError, error } = useQuery({
    queryKey: ['product', productId],
    queryFn: () => apiFetch<Product>(`/api/v1/products/${productId}`),
    enabled: Number.isFinite(productId),
  })

  const addMutation = useMutation({
    mutationFn: async () => {
      if (!token) throw new Error('Giriş gerekli')
      return apiFetch<Cart>('/api/v1/cart', {
        method: 'POST',
        body: JSON.stringify({ productId, quantity: qty }),
      })
    },
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['cart'] })
      toast.success('Sepete eklendi')
    },
    onError: (e) => {
      const msg = e instanceof ApiError ? e.message : 'Sepete eklenemedi'
      toast.error(msg)
    },
  })

  if (!Number.isFinite(productId)) {
    return <p className="text-center text-muted-foreground">Geçersiz ürün</p>
  }

  if (isLoading) {
    return (
      <div className="grid gap-8 lg:grid-cols-2">
        <Skeleton className="aspect-square max-h-[420px] rounded-2xl" />
        <div className="space-y-4">
          <Skeleton className="h-10 w-[66%]" />
          <Skeleton className="h-24 w-full" />
        </div>
      </div>
    )
  }

  if (isError || !product) {
    return (
      <Card className="mx-auto max-w-lg p-8 text-center">
        <p className="text-destructive">{(error as Error)?.message ?? 'Ürün bulunamadı'}</p>
        <Button asChild className="mt-4">
          <Link to="/">Kataloga dön</Link>
        </Button>
      </Card>
    )
  }

  const canBuy = product.stockQuantity > 0

  return (
    <div className="grid gap-10 lg:grid-cols-2 lg:gap-12">
      <div className="space-y-3">
        <div className="relative aspect-square max-h-[480px] overflow-hidden rounded-2xl border border-border/80 shadow-inner">
          <ProductImage
            src={product.imageUrl}
            alt={product.name}
            className="size-full max-h-[480px]"
            fallbackLetter={product.name}
          />
          {!canBuy ? (
            <Badge className="absolute right-4 top-4 shadow-md" variant="secondary">
              Stokta yok
            </Badge>
          ) : null}
        </div>
        {product.imageUrl ? (
          <p className="text-xs text-muted-foreground">
            Görsel kaynağı:{' '}
            <a
              href={product.imageUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="break-all font-medium text-primary underline-offset-2 hover:underline"
            >
              {product.imageUrl}
            </a>
          </p>
        ) : null}
      </div>

      <div className="space-y-6">
        <div>
          <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">{product.sku}</p>
          <h1 className="mt-1 font-display text-3xl font-bold tracking-tight sm:text-4xl">{product.name}</h1>
          <p className="mt-4 text-muted-foreground">{product.description}</p>
        </div>

        <Separator />

        <div className="flex flex-wrap items-end gap-6">
          <div>
            <p className="text-sm text-muted-foreground">Fiyat</p>
            <p className="font-display text-3xl font-bold text-primary">{formatTry(product.price)}</p>
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Stok</p>
            <p className="text-lg font-medium">{product.stockQuantity} adet</p>
          </div>
        </div>

        <Card>
          <CardContent className="space-y-4 pt-6">
            {token ? (
              <>
                <div className="space-y-2">
                  <Label htmlFor="qty">Adet</Label>
                  <div className="flex max-w-[200px] items-center gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      size="icon"
                      disabled={qty <= 1}
                      onClick={() => setQty((q) => Math.max(1, q - 1))}
                    >
                      <Minus className="size-4" />
                    </Button>
                    <Input
                      id="qty"
                      type="number"
                      min={1}
                      max={Math.min(999, product.stockQuantity)}
                      className="text-center"
                      value={qty}
                      onChange={(e) => {
                        const v = Number(e.target.value)
                        if (Number.isNaN(v)) return
                        setQty(Math.min(Math.max(1, v), product.stockQuantity || 1))
                      }}
                    />
                    <Button
                      type="button"
                      variant="outline"
                      size="icon"
                      disabled={qty >= product.stockQuantity}
                      onClick={() => setQty((q) => Math.min(product.stockQuantity, q + 1))}
                    >
                      <Plus className="size-4" />
                    </Button>
                  </div>
                </div>
                <Button
                  className="w-full sm:w-auto"
                  size="lg"
                  disabled={!canBuy || addMutation.isPending}
                  onClick={() => addMutation.mutate()}
                >
                  <ShoppingCart className="size-5" />
                  Sepete ekle
                </Button>
              </>
            ) : (
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                <p className="text-sm text-muted-foreground">Sepete eklemek için giriş yapın.</p>
                <Button onClick={() => navigate('/login', { state: { from: `/products/${product.id}` } })}>
                  Giriş yap
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
