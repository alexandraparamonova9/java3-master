import { createRouter, createWebHistory } from 'vue-router'
import ForbiddenPage from '../pages/ForbiddenPage.vue'
import { useAuthStore } from '../stores/authStore.js'
import NotFoundPage from '../pages/NotFoundPage.vue'
import LoginPage from '../pages/LoginPage.vue'
import ProfilePage from '../pages/ProfilePage.vue'
import RegisterPage from '../pages/RegisterPage.vue'
const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        component: LoginPage,
        meta: { guestOnly: true }
    },
    {
        path: '/register',
        component: RegisterPage,
        meta: { guestOnly: true }
    },
    {
        path: '/profile',
        component: ProfilePage,
        meta: { requiresAuth: true }
    },
    {
        path: '/students',
        component: {
            template: '<h1>Страница студентов</h1>'
        },
        meta: { requiresAuth: true }
    },
    {
        path: '/403',
        component: ForbiddenPage
    },
    {
        path: '/:pathMatch(.*)*',
        component: NotFoundPage
    }
]
const router = createRouter({
    history: createWebHistory(),
    routes
})
router.beforeEach((to) => {
    const auth = useAuthStore()

    // Если пользователь не вошёл — отправляем на страницу входа
    if (to.meta.requiresAuth && !auth.isAuthenticated) {
        return '/login'
    }

    // Если у пользователя нет нужной роли — ошибка 403
    if (to.meta.roles && !to.meta.roles.includes(auth.user?.role)) {
        return '/403'
    }

    // Если пользователь уже вошёл, не показываем ему вход и регистрацию
    if (to.meta.guestOnly && auth.isAuthenticated) {
        return '/students'
    }
})

export default router