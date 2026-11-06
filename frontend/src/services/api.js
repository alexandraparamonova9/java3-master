import { useAuthStore } from '../stores/authStore.js'

export async function apiRequest(path, options = {}) {
    const auth = useAuthStore()

    async function sendRequest() {
        const headers = {
            ...options.headers
        }

        if (auth.accessToken) {
            headers.Authorization = `Bearer ${auth.accessToken}`
        }

        return fetch(path, {
            ...options,
            headers
        })
    }

    let response = await sendRequest()

    if (response.status === 401 && auth.refreshToken &&
        path !== '/api/auth/refresh') {

        try {
            await auth.refresh()
            response = await sendRequest()
        } catch (error) {
            auth.logout()
            throw new Error('Сессия истекла. Войдите снова.')
        }
    }

    if (!response.ok) {
        throw new Error(`Ошибка сервера: ${response.status}`)
    }

    if (response.status === 204) {
        return null
    }

    return response.json()
}