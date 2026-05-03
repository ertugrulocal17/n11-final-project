export type ApiErrorBody = {
  timestamp?: string
  status: number
  error: string
  message: string
  path: string
}

export type Product = {
  id: number
  sku: string
  name: string
  description: string
  /** Tam HTTPS görsel adresi; yoksa yer tutucu kullanılır */
  imageUrl?: string | null
  price: string
  stockQuantity: number
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
}

export type UserProfile = {
  id: number
  email: string
  role: string
}

export type CartLine = {
  productId: number
  sku: string
  name: string
  imageUrl?: string | null
  unitPrice: string
  quantity: number
  lineTotal: string
}

export type Cart = {
  lines: CartLine[]
  subtotal: string
  totalQuantity: number
}

export type OrderStatus = 'CREATED' | 'PAID' | 'CANCELLED'

export type OrderLine = {
  productId: number
  productSku: string
  productName: string
  imageUrl?: string | null
  unitPrice: string
  quantity: number
  lineTotal: string
}

export type OrderSummary = {
  id: number
  status: OrderStatus
  totalAmount: string
  createdAt: string
}

export type OrderDetail = {
  id: number
  status: OrderStatus
  totalAmount: string
  createdAt: string
  lines: OrderLine[]
}

export type PaymentInitialize = {
  token: string
  paymentPageUrl: string
  checkoutFormContent: string
  conversationId: string
}
