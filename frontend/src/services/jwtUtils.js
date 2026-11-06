
export function getUserFromToken(token) {
    try {
        const payload = token.split('.')[1]

        const base64 = payload
            .replace(/-/g, '+')
            .replace(/_/g, '/')

        const data = JSON.parse(atob(base64))

        return {
            username: data.sub,
            role: data.role
        }
    } catch {
        return null
    }
}
