import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '../api/cart'
import type { Cart } from '../types'
import { useAuthStore } from './auth'

export const useCartStore = defineStore('cart', () => {
  const cart = ref<Cart | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const itemCount = computed(() => cart.value?.totalItems || 0)
  const subtotal = computed(() => cart.value?.subtotal || 0)

  async function fetchCart() {
    const authStore = useAuthStore()
    if (!authStore.user) return

    loading.value = true
    error.value = null

    try {
      cart.value = await cartApi.getCart(authStore.user.id)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to fetch cart'
      cart.value = { userId: authStore.user.id, items: [], subtotal: 0, totalItems: 0 }
    } finally {
      loading.value = false
    }
  }

  async function addItem(productId: number, quantity: number) {
    const authStore = useAuthStore()
    if (!authStore.user) return

    loading.value = true
    error.value = null

    try {
      cart.value = await cartApi.addItem(authStore.user.id, productId, quantity)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to add item'
    } finally {
      loading.value = false
    }
  }

  async function updateItem(productId: number, quantity: number) {
    const authStore = useAuthStore()
    if (!authStore.user) return

    loading.value = true
    error.value = null

    try {
      cart.value = await cartApi.updateItem(authStore.user.id, productId, quantity)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to update item'
    } finally {
      loading.value = false
    }
  }

  async function removeItem(productId: number) {
    const authStore = useAuthStore()
    if (!authStore.user) return

    loading.value = true
    error.value = null

    try {
      cart.value = await cartApi.removeItem(authStore.user.id, productId)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to remove item'
    } finally {
      loading.value = false
    }
  }

  async function clearCart() {
    const authStore = useAuthStore()
    if (!authStore.user) return

    loading.value = true
    error.value = null

    try {
      await cartApi.clearCart(authStore.user.id)
      cart.value = { userId: authStore.user.id, items: [], subtotal: 0, totalItems: 0 }
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to clear cart'
    } finally {
      loading.value = false
    }
  }

  return {
    cart,
    loading,
    error,
    itemCount,
    subtotal,
    fetchCart,
    addItem,
    updateItem,
    removeItem,
    clearCart,
  }
})
