<script setup>
defineProps({
  message: {
    type: String,
    required: true
  }
})
</script>

<template>
  <p v-if="message" class="error-message" role="alert">
    {{ message }}
  </p>
</template>