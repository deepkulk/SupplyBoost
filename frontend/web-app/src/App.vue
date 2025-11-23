<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'
import AppHeader from './components/AppHeader.vue'

const authStore = useAuthStore()
const cartStore = useCartStore()

onMounted(() => {
  authStore.initializeAuth()
  if (authStore.isAuthenticated) {
    cartStore.fetchCart()
  }
})
</script>

<template>
  <div id="app">
    <AppHeader />
    <main class="main-content">
      <RouterView />
    </main>
    <footer class="app-footer">
      <p>&copy; 2025 SupplyBoost. All rights reserved.</p>
    </footer>
  </div>
</template>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: #f8f9fa;
  color: #333;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.app-footer {
  background: #333;
  color: white;
  text-align: center;
  padding: 2rem;
  margin-top: 4rem;
}

.app-footer p {
  margin: 0;
}
</style>
