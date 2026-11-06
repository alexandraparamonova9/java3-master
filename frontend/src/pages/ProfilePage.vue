
<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore.js'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="profile-page">
    <h1>Личный кабинет</h1>
    <p>Логин: {{ auth.user?.username }}</p>
    <p>Роль: {{ auth.user?.role }}</p>
    <p>Статус: {{ auth.isAuthenticated ? 'Авторизован' : 'Не авторизован' }}</p>

    <button @click="handleLogout">
      Выйти из аккаунта
    </button>
  </div>
</template>
