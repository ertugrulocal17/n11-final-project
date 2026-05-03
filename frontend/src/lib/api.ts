import type { ApiErrorBody } from '@/types/models'

const STORAGE_KEY = 'n11_access_token'

export function getStoredToken(): string | null {
  return localStorage.getItem(STORAGE_KEY)
}

export function setStoredToken(token: string | null) {
  if (token) localStorage.setItem(STORAGE_KEY, token)
  else localStorage.removeItem(STORAGE_KEY)
}

/** Dev: boş bırakın (Vite proxy). Prod: örn. http://localhost:8080 */
export function apiBase(): string {
  const v = import.meta.env.VITE_API_BASE_URL as string | undefined
  return v?.replace(/\/$/, '') ?? ''
}

export class ApiError extends Error {
  status: number
  body: ApiErrorBody | null

  constructor(message: string, status: number, body: ApiErrorBody | null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

type FetchOpts = RequestInit & { token?: string | null }

export async function apiFetch<T>(path: string, opts: FetchOpts = {}): Promise<T> {
  const { token, headers: h, ...rest } = opts
  const headers = new Headers(h)
  if (!headers.has('Content-Type') && rest.body && !(rest.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  const t = token !== undefined ? token : getStoredToken()
  if (t) headers.set('Authorization', `Bearer ${t}`)

  const url = `${apiBase()}${path.startsWith('/') ? path : `/${path}`}`
  const res = await fetch(url, { ...rest, headers })

  if (res.status === 204) return undefined as T

  const text = await res.text()
  let json: unknown = null
  if (text) {
    try {
      json = JSON.parse(text) as unknown
    } catch {
      /* plain text */
    }
  }

  if (!res.ok) {
    const body =
      json && typeof json === 'object' && 'message' in json ? (json as ApiErrorBody) : null
    const msg =
      body && typeof body.message === 'string' ? body.message : res.statusText || 'İstek başarısız'
    throw new ApiError(msg, res.status, body)
  }

  return json as T
}
