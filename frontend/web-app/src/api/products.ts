import apiClient from './client'
import type { Product } from '../types'

const PRODUCT_SERVICE = '/api/v1/products'

export const productsApi = {
  getAll(): Promise<Product[]> {
    return apiClient.get(PRODUCT_SERVICE).then((res) => res.data)
  },

  getById(id: number): Promise<Product> {
    return apiClient.get(`${PRODUCT_SERVICE}/${id}`).then((res) => res.data)
  },

  search(query: string): Promise<Product[]> {
    return apiClient
      .get(`${PRODUCT_SERVICE}/search`, { params: { q: query } })
      .then((res) => res.data)
  },
}
