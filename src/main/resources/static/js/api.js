// Предпросмотр JetBrains раздаёт HTML на 63342, а API работает в Spring Boot на 8080.
// При обычном запуске через Spring Boot запросы остаются на текущем сервере.
const isIdePreview = window.location.port === '63342'
    && ['localhost', '127.0.0.1'].includes(window.location.hostname);
const API_ORIGIN = isIdePreview ? `http://${window.location.hostname}:8080` : '';
const API_URL = `${API_ORIGIN}/api/students`;
const AUTH_URL = `${API_ORIGIN}/api/auth`;
const LOGIN_PAGE = 'login.html';
const CONNECTION_ERROR = isIdePreview
    ? 'Не удалось подключиться к серверу. Запустите Spring Boot на порту 8080 и повторите попытку.'
    : 'Не удалось подключиться к серверу. Проверьте, что Spring Boot запущен, и повторите попытку.';

// ---------- Токены ----------

function getAccessToken() {
    return localStorage.getItem('accessToken');
}

function getRefreshToken() {
    return localStorage.getItem('refreshToken');
}

// Только для доступности кнопок; права на запросы проверяет сервер.
function isAdmin() {
    try {
        const payload = getAccessToken().split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(atob(payload)).role === 'ADMIN';
    } catch (error) {
        return false;
    }
}

function saveTokens(data) {
    localStorage.setItem('accessToken', data.accessToken);
    if (data.refreshToken) {
        localStorage.setItem('refreshToken', data.refreshToken);
    }
}

function clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
}

function goToLogin() {
    clearTokens();
    window.location.href = LOGIN_PAGE;
}

// Получить новый access-токен по refresh-токену
async function refreshAccessToken() {
    const refreshToken = getRefreshToken();

    if (!refreshToken) {
        return false;
    }

    try {
        const response = await fetch(`${AUTH_URL}/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });

        if (response.status === 400 || response.status === 401) {
            return false;
        }

        if (!response.ok) {
            throw new Error(`Не удалось обновить авторизацию (код ${response.status}). Повторите попытку.`);
        }

        saveTokens(await response.json());
        return true;
    } catch (error) {
        if (error instanceof TypeError) {
            throw new Error('Не удалось подключиться к серверу. Авторизация сохранена, повторите попытку.');
        }
        throw error;
    }
}

// ---------- Запросы ----------

async function apiRequest(url, options = {}, canRetry = true) {
    try {
        const headers = { ...(options.headers || {}) };
        const token = getAccessToken();

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, { ...options, headers });

        // Токен истёк или его нет: пробуем обновить, иначе отправляем на вход
        if (response.status === 401) {
            if (canRetry && await refreshAccessToken()) {
                return apiRequest(url, options, false);
            }
            goToLogin();
            throw new Error('Необходима авторизация');
        }

        if (response.status === 204) {
            return null;
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            const message = data?.error?.message
                || data?.message
                || response.statusText
                || 'Неизвестная ошибка';

            const error = new Error(`Код ${response.status}: ${message}`);
            error.status = response.status;
            error.details = data?.error?.details || null;
            throw error;
        }

        return data;
    } catch (error) {
        if (error instanceof TypeError) {
            throw new Error(CONNECTION_ERROR);
        }

        throw error;
    }
}

// Получение списка студентов с серверной фильтрацией
async function getStudents(filters = {}) {
    const params = new URLSearchParams();

    for (const [key, value] of Object.entries(filters)) {
        if (value !== '' && value !== null && value !== undefined) {
            params.append(key, value);
        }
    }

    if (!params.has('page')) {
        params.set('page', '0');
    }

    if (!params.has('size')) {
        params.set('size', '20');
    }

    const students = [];
    let result;
    do {
        result = await apiRequest(`${API_URL}/search?${params.toString()}`);
        students.push(...result.content);
        params.set('page', String(result.number + 1));
        // Существующий список показывает все результаты без изменения интерфейса.
    } while (filters.page === undefined && !result.last);

    return students;
}

// Получение одного студента
function getStudentById(id) {
    return apiRequest(`${API_URL}/${id}`);
}

// Добавление студента
function addStudent(student) {
    return apiRequest(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(student)
    });
}

// Полное обновление студента
function updateStudent(id, student) {
    return apiRequest(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(student)
    });
}

// Удаление студента
function deleteStudent(id) {
    return apiRequest(`${API_URL}/${id}`, {
        method: 'DELETE'
    });
}

// Кнопка «Выйти» (если есть на странице)
document.addEventListener('DOMContentLoaded', () => {
    const logoutButton = document.getElementById('logoutButton');

    if (logoutButton) {
        logoutButton.addEventListener('click', goToLogin);
    }
});
