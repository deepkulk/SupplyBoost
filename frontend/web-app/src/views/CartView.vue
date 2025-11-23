<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()

onMounted(() => {
  cartStore.fetchCart()
})

function updateQuantity(productId: number, quantity: number) {
  if (quantity <= 0) {
    cartStore.removeItem(productId)
  } else {
    cartStore.updateItem(productId, quantity)
  }
}

function removeItem(productId: number) {
  cartStore.removeItem(productId)
}

function proceedToCheckout() {
  router.push('/checkout')
}
</script>

<template>
  <div class="cart">
    <h1>Shopping Cart</h1>

    <div v-if="cartStore.loading" class="loading">Loading cart...</div>

    <div v-else-if="cartStore.cart && cartStore.cart.items.length > 0" class="cart-content">
      <div class="cart-items">
        <div v-for="item in cartStore.cart.items" :key="item.productId" class="cart-item">
          <div class="item-info">
            <h3>{{ item.productName }}</h3>
            <p class="sku">SKU: {{ item.productSku }}</p>
            <p class="price">${{ item.price.toFixed(2) }}</p>
          </div>

          <div class="item-actions">
            <div class="quantity-control">
              <button
                @click="updateQuantity(item.productId, item.quantity - 1)"
                class="btn-qty"
              >
                -
              </button>
              <span class="quantity">{{ item.quantity }}</span>
              <button
                @click="updateQuantity(item.productId, item.quantity + 1)"
                class="btn-qty"
              >
                +
              </button>
            </div>

            <p class="item-total">${{ (item.price * item.quantity).toFixed(2) }}</p>

            <button @click="removeItem(item.productId)" class="btn-remove">Remove</button>
          </div>
        </div>
      </div>

      <div class="cart-summary">
        <h2>Order Summary</h2>
        <div class="summary-row">
          <span>Subtotal ({{ cartStore.itemCount }} items)</span>
          <span>${{ cartStore.subtotal.toFixed(2) }}</span>
        </div>
        <div class="summary-row">
          <span>Shipping</span>
          <span>Calculated at checkout</span>
        </div>
        <div class="summary-row total">
          <span>Total</span>
          <span>${{ cartStore.subtotal.toFixed(2) }}</span>
        </div>

        <button @click="proceedToCheckout" class="btn btn-primary btn-large">
          Proceed to Checkout
        </button>

        <button @click="router.push('/products')" class="btn btn-secondary btn-large">
          Continue Shopping
        </button>
      </div>
    </div>

    <div v-else class="empty-cart">
      <p>Your cart is empty</p>
      <button @click="router.push('/products')" class="btn btn-primary">
        Browse Products
      </button>
    </div>
  </div>
</template>

<style scoped>
.cart {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  margin-bottom: 2rem;
}

.loading,
.empty-cart {
  text-align: center;
  padding: 3rem;
}

.empty-cart p {
  color: #666;
  font-size: 1.125rem;
  margin-bottom: 1.5rem;
}

.cart-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

@media (max-width: 768px) {
  .cart-content {
    grid-template-columns: 1fr;
  }
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
}

.item-info h3 {
  margin: 0 0 0.5rem 0;
}

.sku {
  color: #999;
  font-size: 0.9rem;
  margin: 0.25rem 0;
}

.price {
  color: #667eea;
  font-weight: bold;
  margin: 0.5rem 0 0 0;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-qty {
  width: 32px;
  height: 32px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.125rem;
}

.btn-qty:hover {
  background: #f5f5f5;
}

.quantity {
  min-width: 40px;
  text-align: center;
  font-weight: 500;
}

.item-total {
  font-weight: bold;
  min-width: 80px;
  text-align: right;
}

.btn-remove {
  background: none;
  border: none;
  color: #dc3545;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-remove:hover {
  text-decoration: underline;
}

.cart-summary {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: fit-content;
}

.cart-summary h2 {
  margin: 0 0 1.5rem 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
  color: #666;
}

.summary-row.total {
  font-size: 1.25rem;
  font-weight: bold;
  color: #333;
  padding-top: 1rem;
  border-top: 2px solid #eee;
  margin-top: 1rem;
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
  margin-top: 1rem;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #f5f5ff;
}
</style>
