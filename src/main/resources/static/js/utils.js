export async function fetchWithAuthCheck(url, options = {}) {
    try {
        const response = await fetch(url, options);
        if (response.status === 401 || response.status === 403) {
            alert('로그인이 필요합니다.');
            window.location.href = '/index.html';
            return null;
        }
        if (!response.ok) {
            throw new Error('요청 실패: ' + response.status);
        }
        return await response.json();
    } catch (error) {
        alert('네트워크 오류 발생: ' + error.message);
        return null;
    }
}
