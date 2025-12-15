import React, { useState, useEffect } from 'react';
import styles from './SubPage.module.css';
/* global authFetch */

function parseJwt(token) {
  try {
    const payload = token.split('.')[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

function getCurrentQuarterRange() {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  const quarter = Math.floor(month / 3) + 1;
  return { year, quarter };
}

const WallRecordSystem = () => {
  const [userId, setUserId] = useState('');
  const [userPw, setUserPw] = useState('');
  const [loggedIn, setLoggedIn] = useState(false);
  const [loginMessageVisible, setLoginMessageVisible] = useState(false);
  const [stats, setStats] = useState([]);
  const [logoutTimerId, setLogoutTimerId] = useState(null);
  const [notices, setNotices] = useState([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedNotice, setSelectedNotice] = useState(null);

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
    const { year, quarter } = getCurrentQuarterRange();
    try {
      const response = await fetch(`/api/statistics/byQuarter?year=${year}&quarter=${quarter}`);
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

    async function fetchNotices() {
          try {
            const res = await authFetch('/api/notices');
            if (!res.ok) throw new Error('공지사항 로드 실패');
            const data = await res.json();
            setNotices(data.slice(0, 10));
          } catch {
            setNotices([]);
          }
        }
    fetchNotices();
    loadStatistics();
  }, []);

    const showNoticeModal = (notice) => {
      setSelectedNotice(notice);
      setModalVisible(true);
    };

    const hideNoticeModal = () => {
      setModalVisible(false);
      setSelectedNotice(null);
    };

  const { year, quarter } = getCurrentQuarterRange();

  const top5ByCount = [...stats]
    .sort((a, b) => b.totalCount - a.totalCount)
    .slice(0, 5);

  const top5TotalCountSum = top5ByCount.reduce((sum, stat) => sum + stat.totalCount, 0);
  const top5TotalCountAvg = top5ByCount.length > 0 ? top5TotalCountSum / top5ByCount.length : 0;
  const resultValue = Math.floor(top5TotalCountAvg * 0.3);
  const filteredStats = stats.filter(stat => stat.totalCount >= resultValue);

  const getAverageRank = (stat) => {
    if (!stat || !stat.totalCount) return 0;
    return (
      (
        stat.firstPlaceCount * 1 +
        stat.secondPlaceCount * 2 +
        stat.thirdPlaceCount * 3 +
        stat.fourthPlaceCount * 4
      ) / stat.totalCount
    );
  };

  return (
      <div className="container">
      <h1 style={{ display: 'flex', alignItems: 'center', gap: '10px', marginLeft: 50 }}>
        <img src="/logo.png" alt="logo" style={{ height: '60px', width: '40px' }} />
        2025년 11월 22일 정모 기록
        <img src="/logo.png" alt="logo" style={{ height: '60px', width: '40px' }} />
      </h1>

      {!loggedIn && (
        <div id="loginForm" className="loginForm" style={{ marginLeft: 50}}>
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
        <div id="btnContainer" className="btnContainer" style={{ marginLeft: 50}}>
          <a href="addmember.html"> <button type="button">멤버 등록</button> </a>
          <a href="addRecord.html"> <button type="button">기록 입력</button> </a>
          <a href="recordList.html"> <button type="button">전체 기록 조회</button> </a>
          <a> <button type="button" id="logoutBtn" onClick={handleLogout}>로그아웃</button> </a>
        </div>
      )}

      {loginMessageVisible && (
        <div id="loginMessage" className="loginMessage" style={{ marginLeft: 50}}>
          로그인 후 기록 입력이 가능합니다.
        </div>
      )}

      <div id="alwaysVisibleBtnContainer" style={{ marginTop: 10, marginLeft: 50 }}>
        <a href="periodList.html"> <button type="button">기간별 기록 조회</button> </a>
        <a href="yakumanList.html"> <button type="button">역만 기록 조회</button> </a>
        <a href="relativeRecord.html"> <button type="button">상대 전적 조회</button> </a>
      </div>

      <div className="table-info" style={{ maxWidth: 950, marginTop: 30, marginLeft: 50, marginRight: 'auto' }}>
        <h2>테이블 정보</h2>
        <table id="noticeTable">
          <thead>
            <tr>
              <th style={{ width: 70 }}>번호</th>
              <th>제목</th>
              <th style={{ width: 100 }}>날짜</th>
            </tr>
          </thead>
          <tbody>
            {notices.length === 0 ? (
              <tr>
                <td colSpan={3}>등록된 공지글이 없습니다.</td>
              </tr>
            ) : (
              notices.map((notice) => (
                <tr key={notice.id} style={{ cursor: 'pointer' }} onClick={() => showNoticeModal(notice)}>
                  <td>{notice.id}</td>
                  <td className="nickname-cell">{notice.title}</td>
                  <td>{notice.date}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

            {modalVisible && selectedNotice && (
              <div
                onClick={hideNoticeModal}
                style={{
                  position: 'fixed',
                  top: 0,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  backgroundColor: 'rgba(0,0,0,0.4)',
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  zIndex: 1000,
                }}
              >
                <div
                  onClick={(e) => e.stopPropagation()}
                  style={{
                    backgroundColor: 'white',
                    padding: 20,
                    borderRadius: 8,
                    minWidth: 300,
                    maxWidth: '90%',
                    maxHeight: '80%',
                    overflowY: 'auto',
                    boxShadow: '0 4px 10px rgba(0,0,0,0.3)',
                  }}
                >
                  <h4 style={{ marginBottom: 12 }}>{selectedNotice.title}</h4>
                  <div style={{ whiteSpace: 'pre-wrap' }}>{selectedNotice.content}</div>
                </div>
              </div>
            )}

      <div className="ranking-table" style={{ maxWidth: 950, marginTop: 30, marginLeft: 50, marginRight: 'auto' }}>
      <h2 style={{ marginTop: 30 }}>
        정모 기록 & 순위
      </h2>
      <table id="rankingTable">
        <thead>
          <tr>
            <th>순위</th>
            <th>닉네임</th>
            <th>총승점</th>
            <th>평균승점</th>
            <th>평균순위</th>
            <th>1위횟수 (1위율)</th>
            <th>2위횟수 (2위율)</th>
            <th>3위횟수 (3위율)</th>
            <th>4위횟수 (4위율)</th>
            <th>총국수</th>
          </tr>
        </thead>
        <tbody>
          {stats.length === 0 ? (
            <tr>
              <td colSpan={9}>기록이 없습니다.</td>
            </tr>
          ) : (
            stats.map((stat, idx) => (
              <tr key={stat.nickname}>
                <td>{idx + 1}</td>
                <td className="nickname-cell">
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
                <td>{getAverageRank(stat).toFixed(2)}</td>
                <td>{stat.firstPlaceCount} ({(stat.firstPlaceRate * 100).toFixed(1)}%)</td>
                <td>{stat.secondPlaceCount} ({(stat.secondPlaceRate * 100).toFixed(1)}%)</td>
                <td>{stat.thirdPlaceCount} ({(stat.thirdPlaceRate * 100).toFixed(1)}%)</td>
                <td>{stat.fourthPlaceCount} ({(stat.fourthPlaceRate * 100).toFixed(1)}%)</td>
                <td>{stat.totalCount}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
      </div>

      <footer>
        Copyright &copy; Cudo{' '}
        <a
          href="https://twitter.com/Cudo3399"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Twitter @Cudo3399"
        >
          <i className="fab fa-twitter" aria-hidden="true"></i>
        </a>
      </footer>
    </div>
  );
};

export default WallRecordSystem;