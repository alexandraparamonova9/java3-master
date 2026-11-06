import { apiRequest } from './api.js'

export function login(username, password) {
    return apiRequest('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
}

export function register(data) {
    return apiRequest('/api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
}

export async function refreshToken(refreshTokenValue) {
    const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            refreshToken: refreshTokenValue
        })
    })

    if (!response.ok) {
        throw new Error('Не удалось обновить токен')
    }

    return response.json()
}
