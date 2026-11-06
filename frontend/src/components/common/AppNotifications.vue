
<script setup>
import { useObservable } from '../../composables/useObservable.js'
import { notifications$ } from '../../rxjs/notifications.js'

const notification = useObservable(notifications$)
</script>

<template>
  <div
      v-if="notification"
      class="notification"
      :class="notification.type"
      role="status"
  >
    {{ notification.message }}
  </div>
</template>
