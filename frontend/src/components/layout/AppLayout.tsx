import { Header } from '@/components/layout/Header'
import { Link } from 'react-router-dom'
import type { ReactNode } from 'react'

export function AppLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-svh flex-col">
      <Header />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8 sm:px-6">{children}</main>
      <footer className="border-t border-border/60 bg-card py-6 text-center text-xs text-muted-foreground">
        <p>
          Bitirme projesi — Spring Boot + React.{' '}
          <Link to="/" className="text-primary hover:underline">
            Ana sayfa
          </Link>
        </p>
      </footer>
    </div>
  )
}
