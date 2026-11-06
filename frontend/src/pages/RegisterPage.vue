
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore.js'
import AuthForm from '../components/auth/AuthForm.vue'
import AppLoader from '../components/common/AppLoader.vue'
import ErrorMessage from '../components/common/ErrorMessage.vue'
const router = useRouter()
const auth = useAuthStore()
const error = ref('')

async function handleRegister(data) {
  error.value = ''

  try {
    await auth.register(data)
    await router.push('/students')
  } catch (err) {
    error.value = err.message
  }
}
</script>

<template>
  <div class="auth-page">
    <h1>Регистрация</h1>

    <AuthForm
        mode="register"
        :loading="auth.loading"
        @submit="handleRegister"
    />

    <ErrorMessage :message="error" />
    <AppLoader v-if="auth.loading" />

    <RouterLink to="/login">
      Уже есть аккаунт? Войти
    </RouterLink>
  </div>
</template>
