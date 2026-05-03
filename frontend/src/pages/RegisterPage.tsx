import { useAuth } from '@/auth/auth-context'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ApiError } from '@/lib/api'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [pending, setPending] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (password.length < 8) {
      toast.error('Şifre en az 8 karakter olmalı')
      return
    }
    setPending(true)
    try {
      await register(email, password)
      toast.success('Hesap oluşturuldu')
      navigate('/', { replace: true })
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Kayıt başarısız'
      toast.error(msg)
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="mx-auto flex max-w-md flex-col gap-6">
      <Card>
        <CardHeader>
          <CardTitle>Kayıt ol</CardTitle>
          <CardDescription>Şifre en az 8 karakter.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="reg-email">E-posta</Label>
              <Input
                id="reg-email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="reg-password">Şifre</Label>
              <Input
                id="reg-password"
                type="password"
                autoComplete="new-password"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <Button type="submit" className="w-full" disabled={pending}>
              {pending ? 'Kayıt…' : 'Hesap oluştur'}
            </Button>
          </form>
        </CardContent>
      </Card>
      <p className="text-center text-sm text-muted-foreground">
        Zaten hesabınız var mı?{' '}
        <Link to="/login" className="font-medium text-primary hover:underline">
          Giriş yapın
        </Link>
      </p>
    </div>
  )
}
