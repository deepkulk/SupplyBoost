<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ordersApi } from '../api/orders'
import type { Order } from '../types'

const route = useRoute()
const router = useRouter()

const order = ref<Order | null>(null)
const loading = ref(false)

onMounted(async () => {
  const id = Number(route.params.id)
  loading.value = true
  try {
    order.value = await ordersApi.getOrder(id)
  } catch (error) {
    console.error('Failed to load order:', error)
  } finally {
    loading.value = false
  }
})

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
  <div class="order-detail">
    <button @click="router.push('/orders')" class="btn-back">← Back to Orders</button>

    <div v-if="loading" class="loading">Loading order...</div>

    <div v-else-if="order" class="order-content">
      <div class="order-header">
        <div>
          <h1>Order {{ order.orderNumber }}</h1>
          <p class="order-date">Placed on {{ new Date(order.createdAt).toLocaleString() }}</p>
        </div>
        <span :class="['status', getStatusClass(order.status)]">
          {{ order.status.replace(/_/g, ' ') }}
        </span>
      </div>

      <div class="order-sections">
        <section class="section">
          <h2>Order Information</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">Order Number:</span>
              <span class="value">{{ order.orderNumber }}</span>
            </div>
            <div class="info-item">
              <span class="label">Status:</span>
              <span class="value">{{ order.status.replace(/_/g, ' ') }}</span>
            </div>
            <div class="info-item">
              <span class="label">Total Amount:</span>
              <span class="value">${{ order.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">Customer:</span>
              <span class="value">{{ order.customerName }}</span>
            </div>
            <div class="info-item">
              <span class="label">Email:</span>
              <span class="value">{{ order.customerEmail }}</span>
            </div>
          </div>
        </section>

        <section class="section">
          <h2>Shipping Address</h2>
          <address>
            {{ order.shippingAddressLine1 }}<br />
            {{ order.shippingCity }}, {{ order.shippingState }} {{ order.shippingPostalCode }}
          </address>
        </section>

        <section class="section">
          <h2>Order Status Timeline</h2>
          <div class="timeline">
            <div class="timeline-item active">
              <div class="timeline-marker"></div>
              <div class="timeline-content">
                <h4>Order Placed</h4>
                <p>{{ new Date(order.createdAt).toLocaleString() }}</p>
              </div>
            </div>
            <div class="timeline-item" :class="{ active: order.status !== 'CREATED' }">
              <div class="timeline-marker"></div>
              <div class="timeline-content">
                <h4>Payment Confirmed</h4>
                <p v-if="order.status !== 'CREATED'">Processing</p>
                <p v-else>Pending</p>
              </div>
            </div>
            <div class="timeline-item" :class="{ active: order.status === 'SHIPPED' || order.status === 'DELIVERED' }">
              <div class="timeline-marker"></div>
              <div class="timeline-content">
                <h4>Shipped</h4>
                <p v-if="order.status === 'SHIPPED' || order.status === 'DELIVERED'">In transit</p>
                <p v-else>Pending</p>
              </div>
            </div>
            <div class="timeline-item" :class="{ active: order.status === 'DELIVERED' }">
              <div class="timeline-marker"></div>
              <div class="timeline-content">
                <h4>Delivered</h4>
                <p v-if="order.status === 'DELIVERED'">Completed</p>
                <p v-else>Pending</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <div v-else class="error">Order not found</div>
  </div>
</template>

<style scoped>
.order-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.btn-back {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 1rem;
  margin-bottom: 2rem;
  padding: 0.5rem 0;
}

.btn-back:hover {
  text-decoration: underline;
}

.loading,
.error {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.order-header h1 {
  margin: 0 0 0.5rem 0;
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

.order-sections {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section h2 {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.label {
  color: #999;
  font-size: 0.875rem;
  font-weight: 500;
}

.value {
  color: #333;
  font-size: 1rem;
}

address {
  font-style: normal;
  line-height: 1.6;
  color: #666;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.timeline-item {
  display: flex;
  gap: 1rem;
  opacity: 0.4;
  transition: opacity 0.3s;
}

.timeline-item.active {
  opacity: 1;
}

.timeline-marker {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ddd;
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.timeline-item.active .timeline-marker {
  background: #667eea;
}

.timeline-content h4 {
  margin: 0 0 0.25rem 0;
  color: #333;
}

.timeline-content p {
  margin: 0;
  color: #666;
  font-size: 0.875rem;
}
</style>
