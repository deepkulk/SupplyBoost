<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ordersApi } from '../api/orders'
import type { Order } from '../types'

const router = useRouter()
const authStore = useAuthStore()

const orders = ref<Order[]>([])
const loading = ref(false)

onMounted(async () => {
  if (authStore.user) {
    loading.value = true
    try {
      orders.value = await ordersApi.getUserOrders(authStore.user.id)
    } catch (error) {
      console.error('Failed to load orders:', error)
    } finally {
      loading.value = false
    }
  }
})

function viewOrder(orderId: number) {
  router.push(`/orders/${orderId}`)
}

function getStatusClass(status: string) {
  const statusMap: Record<string, string> = {
    CREATED: 'status-created',
    PAYMENT_CONFIRMED: 'status-confirmed',
    SHIPPED: 'status-shipped',
    DELIVERED: 'status-delivered',
    CANCELLED: 'status-cancelled',
  }
  return statusMap[status] || 'status-default'
}
</script>

<template>
  <div class="orders">
    <h1>My Orders</h1>

    <div v-if="loading" class="loading">Loading orders...</div>

    <div v-else-if="orders.length > 0" class="orders-list">
      <div v-for="order in orders" :key="order.id" class="order-card" @click="viewOrder(order.id)">
        <div class="order-header">
          <div>
            <h3>Order {{ order.orderNumber }}</h3>
            <p class="order-date">{{ new Date(order.createdAt).toLocaleDateString() }}</p>
          </div>
          <span :class="['status', getStatusClass(order.status)]">
            {{ order.status.replace(/_/g, ' ') }}
          </span>
        </div>

        <div class="order-details">
          <div class="detail-row">
            <span class="label">Total:</span>
            <span class="value">${{ order.totalAmount.toFixed(2) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Ship to:</span>
            <span class="value">
              {{ order.shippingCity }}, {{ order.shippingState }} {{ order.shippingPostalCode }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      <p>You haven't placed any orders yet</p>
      <button @click="router.push('/products')" class="btn btn-primary">
        Start Shopping
      </button>
    </div>
  </div>
</template>

<style scoped>
.orders {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  margin-bottom: 2rem;
}

.loading,
.empty {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.empty button {
  margin-top: 1.5rem;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.order-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
}

.order-header h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
}

.order-date {
  color: #999;
  margin: 0;
}

.status {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-created {
  background: #e3f2fd;
  color: #1976d2;
}

.status-confirmed {
  background: #fff3e0;
  color: #f57c00;
}

.status-shipped {
  background: #e8f5e9;
  color: #388e3c;
}

.status-delivered {
  background: #e1f5fe;
  color: #0277bd;
}

.status-cancelled {
  background: #ffebee;
  color: #c62828;
}

.status-default {
  background: #f5f5f5;
  color: #666;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
}

.label {
  color: #666;
  font-weight: 500;
}

.value {
  color: #333;
}

.btn {
  padding: 0.75rem 2rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
}
</style>
