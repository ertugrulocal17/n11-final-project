import { useAuth } from '@/auth/auth-context'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { apiFetch } from '@/lib/api'
import type { Cart } from '@/types/models'
import { useQuery } from '@tanstack/react-query'
import { Link, NavLink } from 'react-router-dom'
import { LogOut, ShoppingBag, UserRound } from 'lucide-react'

const navClass = ({ isActive }: { isActive: boolean }) =>
  `text-sm font-medium transition-colors hover:text-primary ${isActive ? 'text-primary' : 'text-muted-foreground'}`

export function Header() {
  const { user, logout, loading } = useAuth()

  const { data: cart } = useQuery({
    queryKey: ['cart'],
    queryFn: () => apiFetch<Cart>('/api/v1/cart'),
    enabled: !!user,
  })

  const qty = cart?.totalQuantity ?? 0

  return (
    <header className="sticky top-0 z-50 border-b border-border/80 bg-card/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-4 px-4 sm:px-6">
        <Link to="/" className="flex shrink-0 items-baseline gap-1.5">
          <span className="font-display text-xl font-extrabold tracking-tight text-primary">n11</span>
          <span className="hidden text-sm font-medium text-muted-foreground sm:inline">commerce</span>
        </Link>

        <nav className="flex items-center gap-5 sm:gap-8">
          <NavLink to="/" className={navClass} end>
            Ürünler
          </NavLink>
          {user ? (
            <>
              <NavLink to="/orders" className={navClass}>
                Siparişlerim
              </NavLink>
              <NavLink to="/cart" className="relative text-muted-foreground hover:text-primary">
                <ShoppingBag className="size-5" aria-hidden />
                <span className="sr-only">Sepet</span>
                {qty > 0 ? (
                  <Badge
                    variant="default"
                    className="absolute -right-2 -top-2 flex size-5 items-center justify-center rounded-full p-0 text-[10px]"
                  >
                    {qty > 99 ? '99+' : qty}
                  </Badge>
                ) : null}
              </NavLink>
            </>
          ) : null}
        </nav>

        <div className="flex items-center gap-2">
          {loading ? (
            <Skeleton className="h-9 w-28 rounded-lg" />
          ) : user ? (
            <>
              <span className="hidden max-w-[140px] truncate text-xs text-muted-foreground sm:inline md:max-w-[200px]">
                <UserRound className="mr-1 inline size-3.5 align-text-bottom" aria-hidden />
                {user.email}
              </span>
              <Button variant="outline" size="sm" onClick={() => logout()}>
                <LogOut className="size-4" />
                <span className="hidden sm:inline">Çıkış</span>
              </Button>
            </>
          ) : (
            <>
              <Button variant="ghost" size="sm" asChild>
                <Link to="/login">Giriş</Link>
              </Button>
              <Button size="sm" asChild>
                <Link to="/register">Kayıt ol</Link>
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
