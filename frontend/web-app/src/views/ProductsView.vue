<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productsApi } from '../api/products'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import type { Product } from '../types'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const products = ref<Product[]>([])
const loading = ref(false)
const searchQuery = ref('')

onMounted(async () => {
  await loadProducts()
})

async function loadProducts() {
  loading.value = true
  try {
    products.value = await productsApi.getAll()
  } catch (error) {
    console.error('Failed to load products:', error)
  } finally {
    loading.value = false
  }
}

async function handleAddToCart(product: Product) {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  await cartStore.addItem(product.id, 1)
}

function viewProduct(productId: number) {
  router.push(`/products/${productId}`)
}
</script>

<template>
  <div class="products">
    <div class="header">
      <h1>Products</h1>
      <div class="search-bar">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search products..."
          class="search-input"
        />
      </div>
    </div>

    <div v-if="loading" class="loading">Loading products...</div>

    <div v-else class="product-grid">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="viewProduct(product.id)"
      >
        <div class="product-image">
          <img
            :src="product.imageUrl || 'https://via.placeholder.com/300x200'"
            :alt="product.name"
          />
        </div>
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <p class="product-description">{{ product.description }}</p>
          <div class="product-footer">
            <span class="price">${{ product.price.toFixed(2) }}</span>
            <button
              @click.stop="handleAddToCart(product)"
              class="btn btn-sm btn-primary"
            >
              Add to Cart
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && products.length === 0" class="empty">
      <p>No products found</p>
    </div>
  </div>
</template>

<style scoped>
.products {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  margin-bottom: 2rem;
}

.header h1 {
  margin-bottom: 1rem;
}

.search-bar {
  max-width: 500px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

.loading,
.empty {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 1.5rem;
}

.product-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  color: #333;
}

.product-description {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
}

.price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #667eea;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.btn-sm {
  padding: 0.5rem 1rem;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
}
</style>
