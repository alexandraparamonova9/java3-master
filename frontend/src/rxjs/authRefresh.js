import { from, shareReplay } from 'rxjs'
import { refreshToken } from '../services/authService.js'

let refreshRequest = null

export function refreshAccessToken(auth) {
    if (!refreshRequest) {
        refreshRequest = from(
            refreshToken(auth.refreshToken)
        ).pipe(
            shareReplay(1)
        )
    }

    return refreshRequest
}

export function resetRefreshRequest() {
    refreshRequest = null
}