import apiClient from './client'
import type { Order, CreateOrderRequest } from '../types'

const ORDER_SERVICE = '/api/v1/orders'

export const ordersApi = {
  createOrder(data: CreateOrderRequest): Promise<Order> {
    return apiClient.post(ORDER_SERVICE, data).then((res) => res.data)
  },

  getOrder(orderId: number): Promise<Order> {
    return apiClient.get(`${ORDER_SERVICE}/${orderId}`).then((res) => res.data)
  },

  getUserOrders(userId: number): Promise<Order[]> {
    return apiClient.get(`${ORDER_SERVICE}/user/${userId}`).then((res) => res.data)
  },
}
