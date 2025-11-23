import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/auth'
import type { User, LoginRequest, RegisterRequest } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  function initializeAuth() {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')

    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
    }
  }

  async function login(credentials: LoginRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.login(credentials)
      token.value = response.token
      user.value = {
        id: response.userId,
        username: response.username,
        email: response.email,
        roles: response.roles,
      }

      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(user.value))

      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Login failed'
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(data: RegisterRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.register(data)
      token.value = response.token
      user.value = {
        id: response.userId,
        username: response.username,
        email: response.email,
        roles: response.roles,
      }

      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(user.value))

      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Registration failed'
      return false
    } finally {
      loading.value = false
    }
  }

  function logout() {
    authApi.logout()
    user.value = null
    token.value = null
  }

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    initializeAuth,
    login,
    register,
    logout,
  }
})
