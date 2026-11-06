
<script setup>
defineProps({
  mode: {
    type: String,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit'])

function handleSubmit(event) {
  const form = event.target

  const data = {
    username: form.username.value,
    password: form.password.value
  }

  if (form.elements.namedItem('email')) {
    data.email = form.elements.namedItem('email').value
  }

  emit('submit', data)
}
</script>

<template>
  <form @submit.prevent="handleSubmit">
    <input
        name="username"
        placeholder="Логин"
        minlength="3"
        maxlength="50"
        required
    />

    <input
        v-if="mode === 'register'"
        name="email"
        type="email"
        placeholder="Email"
        required
    />

    <input
        name="password"
        type="password"
        placeholder="Пароль"
        :minlength="mode === 'register' ? 8 : undefined"
        required
    />

    <button type="submit" :disabled="loading">
      {{ loading ? 'Подождите...' : mode === 'login' ? 'Войти' : 'Зарегистрироваться' }}
    </button>
  </form>
</template>
