<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="app-header">
    <div class="container">
      <div class="logo" @click="router.push('/')">
        <h1>SupplyBoost</h1>
      </div>

      <nav class="nav">
        <router-link to="/" class="nav-link">Home</router-link>
        <router-link to="/products" class="nav-link">Products</router-link>

        <template v-if="authStore.isAuthenticated">
          <router-link to="/cart" class="nav-link cart-link">
            Cart
            <span v-if="cartStore.itemCount > 0" class="badge">{{ cartStore.itemCount }}</span>
          </router-link>
          <router-link to="/orders" class="nav-link">Orders</router-link>
          <div class="user-menu">
            <span class="username">{{ authStore.user?.username }}</span>
            <button @click="handleLogout" class="btn-logout">Logout</button>
          </div>
        </template>

        <template v-else>
          <router-link to="/login" class="nav-link">Login</router-link>
          <router-link to="/register" class="btn-register">Register</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  cursor: pointer;
}

.logo h1 {
  margin: 0;
  font-size: 1.5rem;
  color: #667eea;
}

.nav {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-link {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  transition: color 0.3s;
  position: relative;
}

.nav-link:hover {
  color: #667eea;
}

.nav-link.router-link-active {
  color: #667eea;
}

.cart-link {
  position: relative;
}

.badge {
  position: absolute;
  top: -8px;
  right: -12px;
  background: #dc3545;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 0.75rem;
  font-weight: bold;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  color: #666;
  font-weight: 500;
}

.btn-logout {
  background: none;
  border: 1px solid #ddd;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
}

.btn-logout:hover {
  background: #f5f5f5;
  border-color: #bbb;
}

.btn-register {
  background: #667eea;
  color: white;
  padding: 0.5rem 1.5rem;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-register:hover {
  background: #5568d3;
}

@media (max-width: 768px) {
  .nav {
    gap: 1rem;
  }

  .username {
    display: none;
  }
}
</style>
