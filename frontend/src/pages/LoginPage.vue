
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore.js'
import { showNotification } from '../rxjs/notifications.js'
import AuthForm from '../components/auth/AuthForm.vue'
import AppLoader from '../components/common/AppLoader.vue'
import ErrorMessage from '../components/common/ErrorMessage.vue'
const router = useRouter()
const auth = useAuthStore()
const error = ref('')

async function handleLogin(data) {
  error.value = ''

  try {
        await auth.login(data.username, data.password)

    showNotification('Вход выполнен успешно')

    await router.push('/students')
  } catch (err) {
    error.value = err.message
    showNotification('Ошибка входа', 'error')
  }
}
</script>

<template>
  <div class="auth-page">
    <h1>Вход</h1>

    <AuthForm
        mode="login"
        :loading="auth.loading"
        @submit="handleLogin"
    />

    <ErrorMessage :message="error" />
    <AppLoader v-if="auth.loading" />

    <RouterLink to="/register">
      Зарегистрироваться
    </RouterLink>
  </div>
</template>
