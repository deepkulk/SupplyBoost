export interface User {
  id: number
  username: string
  email: string
  roles: string[]
}

export interface Product {
  id: number
  sku: string
  name: string
  description: string
  price: number
  imageUrl?: string
  stock: number
  category?: string
}

export interface CartItem {
  productId: number
  quantity: number
  price: number
  productName: string
  productSku: string
}

export interface Cart {
  userId: number
  items: CartItem[]
  subtotal: number
  totalItems: number
}

export interface Address {
  line1: string
  line2?: string
  city: string
  state: string
  postalCode: string
  country: string
}

export interface CreateOrderRequest {
  shippingAddress: Address
  billingAddress: Address
  customerName: string
  customerEmail: string
  customerPhone: string
}

export interface Order {
  id: number
  orderNumber: string
  userId: number
  status: string
  totalAmount: number
  customerName: string
  customerEmail: string
  shippingAddressLine1: string
  shippingCity: string
  shippingState: string
  shippingPostalCode: string
  createdAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  type: string
  userId: number
  username: string
  email: string
  roles: string[]
}
