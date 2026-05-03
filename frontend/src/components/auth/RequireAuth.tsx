import { useAuth } from '@/auth/auth-context'
import { Skeleton } from '@/components/ui/skeleton'
import { Navigate, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'

export function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth()
  const loc = useLocation()

  if (loading) {
    return (
      <div className="space-y-4 py-12">
        <Skeleton className="mx-auto h-10 max-w-md" />
        <Skeleton className="mx-auto h-64 max-w-md" />
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: loc.pathname }} />
  }

  return children
}
