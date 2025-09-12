function getAuthToken() {
    const token = localStorage.getItem('authToken');
    return token;
}

async function authFetch(url, options = {}) {

    const token = getAuthToken();
    const headers = options.headers ? { ...options.headers } : {};

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const fetchOptions = {
        ...options,
        headers
    };

    try {
        // 요청 실행
        const response = await fetch(url, fetchOptions);

        if (response.status === 401) {
            alert('세션이 만료되었습니다. 다시 로그인해주세요.');
            window.location.href = '/index.html';
            return;
        }

        return response;
    } catch (error) {
        console.error('authFetch 에러:', error);
        throw error;
    }
}

function parseJwt(token) {
    try {
        const payload = token.split('.')[1];
        const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(decoded);
    } catch (e) {
        return null;
    }
}
