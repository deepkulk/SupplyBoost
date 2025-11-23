<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { ordersApi } from '../api/orders'
import type { Address } from '../types'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const customerName = ref('')
const customerEmail = ref('')
const customerPhone = ref('')

const shippingAddress = ref<Address>({
  line1: '',
  line2: '',
  city: '',
  state: '',
  postalCode: '',
  country: 'USA',
})

const billingAddress = ref<Address>({
  line1: '',
  line2: '',
  city: '',
  state: '',
  postalCode: '',
  country: 'USA',
})

const sameAsShipping = ref(true)
const processing = ref(false)
const error = ref('')

onMounted(() => {
  cartStore.fetchCart()
  if (authStore.user) {
    customerName.value = authStore.user.username
    customerEmail.value = authStore.user.email
  }
})

async function placeOrder() {
  error.value = ''
  processing.value = true

  try {
    const order = await ordersApi.createOrder({
      customerName: customerName.value,
      customerEmail: customerEmail.value,
      customerPhone: customerPhone.value,
      shippingAddress: shippingAddress.value,
      billingAddress: sameAsShipping.value ? shippingAddress.value : billingAddress.value,
    })

    await cartStore.clearCart()
    router.push(`/orders/${order.id}`)
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to place order'
  } finally {
    processing.value = false
  }
}
</script>

<template>
  <div class="checkout">
    <h1>Checkout</h1>

    <div class="checkout-content">
      <div class="checkout-form">
        <section class="form-section">
          <h2>Contact Information</h2>
          <div class="form-group">
            <label for="name">Full Name *</label>
            <input id="name" v-model="customerName" type="text" required />
          </div>
          <div class="form-group">
            <label for="email">Email *</label>
            <input id="email" v-model="customerEmail" type="email" required />
          </div>
          <div class="form-group">
            <label for="phone">Phone *</label>
            <input id="phone" v-model="customerPhone" type="tel" required />
          </div>
        </section>

        <section class="form-section">
          <h2>Shipping Address</h2>
          <div class="form-group">
            <label for="address1">Address Line 1 *</label>
            <input id="address1" v-model="shippingAddress.line1" type="text" required />
          </div>
          <div class="form-group">
            <label for="address2">Address Line 2</label>
            <input id="address2" v-model="shippingAddress.line2" type="text" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="city">City *</label>
              <input id="city" v-model="shippingAddress.city" type="text" required />
            </div>
            <div class="form-group">
              <label for="state">State *</label>
              <input id="state" v-model="shippingAddress.state" type="text" required />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="postal">Postal Code *</label>
              <input id="postal" v-model="shippingAddress.postalCode" type="text" required />
            </div>
            <div class="form-group">
              <label for="country">Country *</label>
              <input id="country" v-model="shippingAddress.country" type="text" required />
            </div>
          </div>
        </section>

        <section class="form-section">
          <div class="checkbox-group">
            <input id="same-address" v-model="sameAsShipping" type="checkbox" />
            <label for="same-address">Billing address same as shipping</label>
          </div>
        </section>

        <div v-if="error" class="error">{{ error }}</div>

        <button
          @click="placeOrder"
          class="btn btn-primary btn-large"
          :disabled="processing || !cartStore.cart?.items.length"
        >
          {{ processing ? 'Processing...' : 'Place Order' }}
        </button>
      </div>

      <div class="order-summary">
        <h2>Order Summary</h2>
        <div v-if="cartStore.cart" class="summary-items">
          <div v-for="item in cartStore.cart.items" :key="item.productId" class="summary-item">
            <span>{{ item.productName }} × {{ item.quantity }}</span>
            <span>${{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>

        <div class="summary-totals">
          <div class="summary-row">
            <span>Subtotal</span>
            <span>${{ cartStore.subtotal.toFixed(2) }}</span>
          </div>
          <div class="summary-row">
            <span>Shipping</span>
            <span>FREE</span>
          </div>
          <div class="summary-row total">
            <span>Total</span>
            <span>${{ cartStore.subtotal.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.checkout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  margin-bottom: 2rem;
}

.checkout-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

@media (max-width: 768px) {
  .checkout-content {
    grid-template-columns: 1fr;
  }
}

.checkout-form {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-section {
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #eee;
}

.form-section:last-child {
  border-bottom: none;
}

.form-section h2 {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.checkbox-group input[type='checkbox'] {
  width: auto;
}

.error {
  color: #dc3545;
  background: #ffe6e6;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.btn {
  padding: 0.75rem 2rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s;
}

.btn-large {
  width: 100%;
  padding: 1rem;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #5568d3;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.order-summary {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: fit-content;
}

.order-summary h2 {
  margin: 0 0 1.5rem 0;
}

.summary-items {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: #666;
}

.summary-totals {
  margin-top: 1.5rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.summary-row.total {
  font-size: 1.25rem;
  font-weight: bold;
  padding-top: 1rem;
  border-top: 2px solid #eee;
  margin-top: 1rem;
}
</style>
