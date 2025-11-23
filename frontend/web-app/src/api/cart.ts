import apiClient from './client'
import type { Cart } from '../types'

const CART_SERVICE = '/api/v1/carts'

export const cartApi = {
  getCart(userId: number): Promise<Cart> {
    return apiClient.get(`${CART_SERVICE}/${userId}`).then((res) => res.data)
  },

  addItem(userId: number, productId: number, quantity: number): Promise<Cart> {
    return apiClient
      .post(`${CART_SERVICE}/${userId}/items`, { productId, quantity })
      .then((res) => res.data)
  },

  updateItem(userId: number, productId: number, quantity: number): Promise<Cart> {
    return apiClient
      .put(`${CART_SERVICE}/${userId}/items/${productId}`, { quantity })
      .then((res) => res.data)
  },

  removeItem(userId: number, productId: number): Promise<Cart> {
    return apiClient
      .delete(`${CART_SERVICE}/${userId}/items/${productId}`)
      .then((res) => res.data)
  },

  clearCart(userId: number): Promise<void> {
    return apiClient.delete(`${CART_SERVICE}/${userId}`).then((res) => res.data)
  },
}
