<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="home">
    <div class="hero">
      <h1>Welcome to SupplyBoost</h1>
      <p>Your one-stop shop for supply chain management solutions</p>
      <div class="actions">
        <button @click="router.push('/products')" class="btn btn-primary">
          Browse Products
        </button>
        <button @click="router.push('/register')" class="btn btn-secondary">
          Get Started
        </button>
      </div>
    </div>

    <div class="features">
      <div class="feature">
        <h3>Wide Selection</h3>
        <p>Browse thousands of products for your business needs</p>
      </div>
      <div class="feature">
        <h3>Fast Shipping</h3>
        <p>Get your orders delivered quickly with real-time tracking</p>
      </div>
      <div class="feature">
        <h3>Secure Payments</h3>
        <p>Shop with confidence using our secure payment system</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.hero {
  text-align: center;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  margin-bottom: 3rem;
}

.hero h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.hero p {
  font-size: 1.25rem;
  margin-bottom: 2rem;
}

.actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-top: 3rem;
}

.feature {
  text-align: center;
  padding: 2rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.feature h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: #667eea;
}

.btn {
  padding: 0.75rem 2rem;
  font-size: 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: white;
  color: #667eea;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-secondary {
  background: transparent;
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background: white;
  color: #667eea;
}
</style>
