
import { Subject } from 'rxjs'

const notifications = new Subject()

export function showNotification(message, type = 'success') {
    notifications.next({
        message,
        type
    })
}

export const notifications$ = notifications.asObservable()

