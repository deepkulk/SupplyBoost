<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productsApi } from '../api/products'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import type { Product } from '../types'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const product = ref<Product | null>(null)
const loading = ref(false)
const quantity = ref(1)

onMounted(async () => {
  const id = Number(route.params.id)
  loading.value = true
  try {
    product.value = await productsApi.getById(id)
  } catch (error) {
    console.error('Failed to load product:', error)
  } finally {
    loading.value = false
  }
})

async function addToCart() {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  if (product.value) {
    await cartStore.addItem(product.value.id, quantity.value)
    router.push('/cart')
  }
}
</script>

<template>
  <div class="product-detail">
    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="product" class="product-content">
      <button @click="router.back()" class="btn-back">← Back to Products</button>

      <div class="product-layout">
        <div class="product-image">
          <img
            :src="product.imageUrl || 'https://via.placeholder.com/600x400'"
            :alt="product.name"
          />
        </div>

        <div class="product-details">
          <h1>{{ product.name }}</h1>
          <p class="sku">SKU: {{ product.sku }}</p>
          <p class="price">${{ product.price.toFixed(2) }}</p>

          <p class="description">{{ product.description }}</p>

          <div class="stock-info">
            <span v-if="product.stock > 0" class="in-stock">
              In Stock ({{ product.stock }} available)
            </span>
            <span v-else class="out-of-stock">Out of Stock</span>
          </div>

          <div class="quantity-selector">
            <label for="quantity">Quantity:</label>
            <input
              id="quantity"
              v-model.number="quantity"
              type="number"
              min="1"
              :max="product.stock"
            />
          </div>

          <button
            @click="addToCart"
            class="btn btn-primary btn-large"
            :disabled="product.stock === 0"
          >
            Add to Cart
          </button>
        </div>
      </div>
    </div>

    <div v-else class="error">Product not found</div>
  </div>
</template>

<style scoped>
.product-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.loading,
.error {
  text-align: center;
  padding: 3rem;
  color: #666;
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

.product-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
}

@media (max-width: 768px) {
  .product-layout {
    grid-template-columns: 1fr;
  }
}

.product-image {
  background: #f5f5f5;
  border-radius: 12px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: auto;
  display: block;
}

.product-details h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.sku {
  color: #999;
  margin-bottom: 1rem;
}

.price {
  font-size: 2.5rem;
  font-weight: bold;
  color: #667eea;
  margin: 1rem 0;
}

.description {
  color: #666;
  line-height: 1.6;
  margin: 1.5rem 0;
}

.stock-info {
  margin: 1.5rem 0;
}

.in-stock {
  color: #28a745;
  font-weight: 500;
}

.out-of-stock {
  color: #dc3545;
  font-weight: 500;
}

.quantity-selector {
  margin: 1.5rem 0;
}

.quantity-selector label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.quantity-selector input {
  width: 100px;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
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
  font-size: 1.125rem;
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
</style>
