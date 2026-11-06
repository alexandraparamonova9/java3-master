
import { ref, onMounted, onUnmounted } from 'vue'

export function useObservable(observable, initialValue = null) {
    const value = ref(initialValue)

    let subscription = null

    onMounted(() => {
        subscription = observable.subscribe((data) => {
            value.value = data
        })
    })

    onUnmounted(() => {
        subscription?.unsubscribe()
    })

    return value
}

