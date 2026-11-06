import { firstValueFrom } from 'rxjs'
import { getUserFromToken } from '../services/jwtUtils.js'
import {
    refreshAccessToken,
    resetRefreshRequest
} from '../rxjs/authRefresh.js'
import { defineStore } from 'pinia'
import { login as loginRequest, register as registerRequest } from '../services/authService.js'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: null,
        refreshToken: null,
        user: null,
        loading: false
    }),

    getters: {
        isAuthenticated: (state) => !!state.accessToken,
        isAdmin: (state) => state.user?.role === 'ADMIN'
    },

    actions: {
        async login(username, password) {
            this.loading = true

            try {
                const response = await loginRequest(username, password)

                this.accessToken = response.accessToken
                this.refreshToken = response.refreshToken

                return response
            } finally {
                this.loading = false
            }
        },

        async register(data) {
            this.loading = true

            try {
                const response = await registerRequest(data)

                this.accessToken = response.accessToken
                this.refreshToken = response.refreshToken

                return response
            } finally {
                this.loading = false
            }
        },
        async refresh() {
            if (!this.refreshToken) {
                throw new Error('Нет refresh-токена')
            }

            try {
                const response = await firstValueFrom(
                    refreshAccessToken(this)
                )

                this.accessToken = response.accessToken
                this.refreshToken = response.refreshToken
                this.user = getUserFromToken(response.accessToken)

                return response.accessToken
            } finally {
                resetRefreshRequest()
            }
        },
        logout() {
            this.accessToken = response.accessToken
            this.refreshToken = response.refreshToken
            this.user = getUserFromToken(response.accessToken)
        }
    }
})
