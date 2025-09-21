import React, { useState, useEffect } from 'react';
import styles from './WallRecordSystem.module.css';

function parseJwt(token) {
  try {
    const payload = token.split('.')[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

const WallRecordSystem = () => {
  const [userId, setUserId] = useState('');
  const [userPw, setUserPw] = useState('');
  const [loggedIn, setLoggedIn] = useState(false);
  const [loginMessageVisible, setLoginMessageVisible] = useState(false);
  const [stats, setStats] = useState([]);
  const [logoutTimerId, setLogoutTimerId] = useState(null);

  const scheduleAutoLogout = (token) => {
    if (logoutTimerId) clearTimeout(logoutTimerId);
    if (!token) return;

    const jwt = parseJwt(token);
    if (jwt && jwt.exp) {
      const nowSec = Math.floor(Date.now() / 1000);
      const expiresInSec = jwt.exp - nowSec;
      localStorage.setItem('tokenExpiresAt', jwt.exp);
      if (expiresInSec > 0) {
        const timerId = setTimeout(() => {
          alert('세션이 만료되어 자동 로그아웃 됩니다.');
          logoutAndUI();
        }, expiresInSec * 1000);
        setLogoutTimerId(timerId);
      } else {
        logoutAndUI();
      }
    }
  };

  const logoutAndUI = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('loginUserId');
    localStorage.removeItem('tokenExpiresAt');
    if (logoutTimerId) {
      clearTimeout(logoutTimerId);
      setLogoutTimerId(null);
    }
    setLoggedIn(false);
    setLoginMessageVisible(true);
  };

  const handleLogin = async () => {
    if (!userId.trim() || !userPw.trim()) {
      alert('아이디와 비밀번호를 모두 입력해주세요.');
      return;
    }

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: userId.trim(), password: userPw.trim() }),
      });

      if (!response.ok) {
        alert('로그인 실패: 아이디 또는 비밀번호가 올바르지 않습니다.');
        return;
      }

      const data = await response.json();
      const token = data.token;
      const username = data.username || userId.trim();

      localStorage.setItem('authToken', token);
      localStorage.setItem('loginUserId', username);

      scheduleAutoLogout(token);
      setLoggedIn(true);
      setLoginMessageVisible(false);
    } catch (error) {
      alert('로그인 중 오류가 발생했습니다: ' + error.message);
    }
  };

  const handleLogout = () => logoutAndUI();

  const loadStatistics = async () => {
    try {
      const response = await fetch('/api/statistics');
      if (!response.ok) throw new Error('통계 데이터 불러오기 실패: ' + response.status);

      let statsData = await response.json();
      statsData.sort((a, b) => b.totalPoints - a.totalPoints);
      setStats(statsData);
    } catch (error) {
      alert(error.message);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('authToken');
    const expiresAt = Number(localStorage.getItem('tokenExpiresAt') || 0);
    const nowSec = Math.floor(Date.now() / 1000);

    if (token && expiresAt) {
      if (expiresAt > nowSec) {
        scheduleAutoLogout(token);
        setLoggedIn(true);
        setLoginMessageVisible(false);
      } else {
        logoutAndUI();
      }
    } else if (token) {
      setLoggedIn(true);
      setLoginMessageVisible(false);
    } else {
      setLoggedIn(false);
      setLoginMessageVisible(true);
    }
    loadStatistics();
  }, []);

  return (
    <div className="container">
      <h1>벽뿌방 기록시스템</h1>

      {!loggedIn && (
        <div id="loginForm" className="loginForm">
          <input
            type="text"
            id="userId"
            name="userId"
            placeholder="ID"
            autoComplete="username"
            className="loginInput userIdPw"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
          <input
            type="password"
            id="userPw"
            name="userPw"
            placeholder="PW"
            autoComplete="current-password"
            className="loginInput userIdPw"
            value={userPw}
            onChange={(e) => setUserPw(e.target.value)}
          />
          <button type="button" id="loginBtn" className="loginBtn" onClick={handleLogin}>
            로그인
          </button>
        </div>
      )}

      {loggedIn && (
        <div id="btnContainer" className="btnContainer">
          <a href="addmember.html"><button type="button">멤버 등록</button></a>
          <a href="addRecord.html"><button type="button">기록 입력</button></a>
          <a href="recordList.html"><button type="button">전체 기록 조회</button></a>
          <button type="button" id="logoutBtn" onClick={handleLogout}>로그아웃</button>
        </div>
      )}

      <div id="alwaysVisibleBtnContainer" style={{ marginTop: 10 }}>
        <a href="periodList.html"><button type="button">기간별 기록 조회</button></a>
        <a href="yakumanList.html"><button type="button">역만 기록 조회</button></a>
      </div>

      {loginMessageVisible && (
        <div id="loginMessage" className="loginMessage">
          로그인 후 기록 입력이 가능합니다.
        </div>
      )}

      <h2 style={{ marginTop: 30 }}>전체 기록</h2>
      <table id="statsTable">
        <thead>
          <tr>
            <th>닉네임</th>
            <th>총승점</th>
            <th>평균승점</th>
            <th>1위횟수 (1위율)</th>
            <th>2위횟수 (2위율)</th>
            <th>3위횟수 (3위율)</th>
            <th>4위횟수 (4위율)</th>
            <th>총국수</th>
          </tr>
        </thead>
        <tbody>
          {stats.map((stat) => (
            <tr key={stat.nickname}>
              <td>
                <a
                  href={`detail.html?nickname=${encodeURIComponent(stat.nickname)}`}
                  className="detail-link"
                  title={`${stat.nickname}의 상세 기록 보기`}
                >
                  {stat.nickname}
                </a>
              </td>
              <td>{stat.totalPoints.toFixed(1)}</td>
              <td>{stat.avgPoints.toFixed(1)}</td>
              <td>{stat.firstPlaceCount} ({(stat.firstPlaceRate * 100).toFixed(1)}%)</td>
              <td>{stat.secondPlaceCount} ({(stat.secondPlaceRate * 100).toFixed(1)}%)</td>
              <td>{stat.thirdPlaceCount} ({(stat.thirdPlaceRate * 100).toFixed(1)}%)</td>
              <td>{stat.fourthPlaceCount} ({(stat.fourthPlaceRate * 100).toFixed(1)}%)</td>
              <td>{stat.totalCount}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <footer>
        Copyright &copy; Cudo{' '}
        <a href="https://twitter.com/Cudo3399" target="_blank" rel="noopener noreferrer" aria-label="Twitter @Cudo3399">
          <i className="fab fa-twitter" aria-hidden="true"></i>
        </a>
      </footer>
    </div>
  );
};

export default WallRecordSystem;