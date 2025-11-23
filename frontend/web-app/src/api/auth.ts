import apiClient from './client'
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types'

const AUTH_SERVICE = '/api/v1/auth'

export const authApi = {
  login(data: LoginRequest): Promise<AuthResponse> {
    return apiClient.post(`${AUTH_SERVICE}/login`, data).then((res) => res.data)
  },

  register(data: RegisterRequest): Promise<AuthResponse> {
    return apiClient.post(`${AUTH_SERVICE}/register`, data).then((res) => res.data)
  },

  logout(): void {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },
}
