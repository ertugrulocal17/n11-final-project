import { ProductImage } from '@/components/product/ProductImage'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { apiFetch } from '@/lib/api'
import { formatTry } from '@/lib/format'
import type { PageResponse, Product } from '@/types/models'
import { useQuery } from '@tanstack/react-query'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'

const PAGE_SIZE = 12

export function HomePage() {
  const [sp, setSp] = useSearchParams()
  const page = Math.max(0, Number(sp.get('page') ?? '0') || 0)

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ['products', page],
    queryFn: () =>
      apiFetch<PageResponse<Product>>(`/api/v1/products?page=${page}&size=${PAGE_SIZE}&sort=id,asc`),
  })

  const setPage = (p: number) => {
    const next = new URLSearchParams(sp)
    if (p <= 0) next.delete('page')
    else next.set('page', String(p))
    setSp(next, { replace: true })
  }

  return (
    <div className="space-y-10">
      <section className="relative overflow-hidden rounded-2xl border border-primary/15 bg-gradient-to-br from-primary/[0.07] via-card to-accent/30 px-6 py-10 shadow-sm sm:px-10 sm:py-14">
        <div className="relative max-w-2xl space-y-3">
          <p className="text-sm font-medium uppercase tracking-widest text-primary">E-ticaret vitrin</p>
          <h1 className="font-display text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Keşfet, sepete ekle, güvenle öde
          </h1>
          <p className="text-muted-foreground">
            Demo katalog; ürün görselleri{' '}
            <a
              href="https://picsum.photos"
              target="_blank"
              rel="noopener noreferrer"
              className="font-medium text-primary underline-offset-4 hover:underline"
            >
              Picsum
            </a>{' '}
            üzerinden örnek bağlantılardır. Giriş sonrası sepet ve ödeme akışını deneyebilirsiniz.
          </p>
        </div>
      </section>

      {isError ? (
        <div className="rounded-xl border border-destructive/30 bg-destructive/5 p-6 text-center text-sm text-destructive">
          {(error as Error).message}
          <Button variant="outline" className="mt-4" onClick={() => void refetch()}>
            Yeniden dene
          </Button>
        </div>
      ) : null}

      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {isLoading
          ? Array.from({ length: 8 }).map((_, i) => (
              <Card key={i} className="overflow-hidden">
                <Skeleton className="aspect-[4/3] w-full rounded-none rounded-t-xl" />
                <CardContent className="space-y-2 pt-4">
                  <Skeleton className="h-4 w-[75%]" />
                  <Skeleton className="h-3 w-1/2" />
                </CardContent>
              </Card>
            ))
          : (data?.content ?? []).map((p) => (
              <Card key={p.id} className="group flex flex-col overflow-hidden transition-shadow hover:shadow-md">
                <Link
                  to={`/products/${p.id}`}
                  className="relative aspect-[4/3] overflow-hidden rounded-t-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
                >
                  <ProductImage
                    src={p.imageUrl}
                    alt={p.name}
                    className="size-full"
                    fallbackLetter={p.name}
                    imgClassName="group-hover:scale-105"
                  />
                  {p.stockQuantity <= 5 && p.stockQuantity > 0 ? (
                    <Badge variant="warning" className="absolute right-2 top-2 shadow-sm">
                      Son {p.stockQuantity}
                    </Badge>
                  ) : null}
                  {p.stockQuantity === 0 ? (
                    <Badge variant="secondary" className="absolute right-2 top-2 shadow-sm">
                      Tükendi
                    </Badge>
                  ) : null}
                </Link>
                <CardContent className="flex flex-1 flex-col pt-4">
                  <p className="text-[10px] font-mono uppercase tracking-wide text-muted-foreground">{p.sku}</p>
                  <Link to={`/products/${p.id}`} className="font-display font-semibold hover:text-primary">
                    {p.name}
                  </Link>
                  <p className="mt-1 line-clamp-2 text-xs text-muted-foreground">{p.description}</p>
                  <p className="mt-auto pt-3 font-display text-lg font-bold text-primary">{formatTry(p.price)}</p>
                  {p.imageUrl ? (
                    <a
                      href={p.imageUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="mt-2 text-[11px] text-primary/80 underline-offset-2 hover:text-primary hover:underline"
                      onClick={(e) => e.stopPropagation()}
                    >
                      Görsel bağlantısı
                    </a>
                  ) : null}
                </CardContent>
                <CardFooter className="border-t border-border/60 bg-muted/30 pt-4">
                  <Button className="w-full" variant="secondary" asChild>
                    <Link to={`/products/${p.id}`}>İncele</Link>
                  </Button>
                </CardFooter>
              </Card>
            ))}
      </div>

      {data && data.totalPages > 1 ? (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            size="icon"
            disabled={page <= 0}
            onClick={() => setPage(page - 1)}
            aria-label="Önceki sayfa"
          >
            <ChevronLeft className="size-4" />
          </Button>
          <span className="min-w-[8rem] text-center text-sm text-muted-foreground">
            Sayfa {page + 1} / {data.totalPages}
          </span>
          <Button
            variant="outline"
            size="icon"
            disabled={page >= data.totalPages - 1}
            onClick={() => setPage(page + 1)}
            aria-label="Sonraki sayfa"
          >
            <ChevronRight className="size-4" />
          </Button>
        </div>
      ) : null}
    </div>
  )
}
