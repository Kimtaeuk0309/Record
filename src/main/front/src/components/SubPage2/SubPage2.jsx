import React, { useState, useEffect } from 'react';
import styles from './SubPage2.module.css';
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

function getTodayDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');

  console.log('Today Date:', { year, month, day });

  return {
    year: parseInt(year),
    month: parseInt(month),
    day: parseInt(day)
  };
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

  const handleLogin = async (e) => {
    e.preventDefault();
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
      setUserId('');
      setUserPw('');
    } catch (error) {
      alert('로그인 중 오류가 발생했습니다: ' + error.message);
    }
  };

  const handleLogout = () => logoutAndUI();

  const loadStatistics = async () => {
    const { year, month, day } = getTodayDate();
    try {
      const response = await fetch(`/api/statistics/v2/byDay?year=${year}&month=${month}&day=${day}`);
      if (!response.ok) throw new Error('통계 데이터 불러오기 실패: ' + response.status);
      let statsData = await response.json();
      statsData.sort((a, b) => b.totalPoints - a.totalPoints);
      setStats(statsData);
    } catch (error) {
      alert(error.message);
    }
  };

  const loadNotices = async () => {
    try {
      const res = await fetch('/api/notices/main');
      if (!res.ok) {
        console.warn('공지사항 로드 실패:', res.status);
        setNotices([]);
        return;
      }
      const data = await res.json();
      setNotices(Array.isArray(data) ? data.slice(0, 10) : []);
    } catch (error) {
      console.error('공지사항 로드 중 오류:', error);
      setNotices([]);
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

    loadNotices();
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
    <div className={styles.container}>
      {/* 헤더 */}
      <h1 style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '10px',
        fontSize: 'clamp(1.3em, 5vw, 1.8em)',
        marginBottom: '20px'
      }}>
        <img src="/logo.png" alt="logo" style={{ height: '50px', width: 'auto' }} />
        또바기들 기록 사이트
        <img src="/logo.png" alt="logo" style={{ height: '50px', width: 'auto' }} />
      </h1>

      {/* 로그인 폼 */}
      {!loggedIn && (
        <form className={styles.loginForm} onSubmit={handleLogin}>
          <input
            type="text"
            placeholder="ID"
            autoComplete="username"
            className={styles.userIdPw}
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="PW"
            autoComplete="current-password"
            className={styles.userIdPw}
            value={userPw}
            onChange={(e) => setUserPw(e.target.value)}
            required
          />
          <button type="submit" className={styles.loginBtn}>
            로그인
          </button>
        </form>
      )}

      {/* 로그인 메시지 */}
      {loginMessageVisible && (
        <div className={styles.loginMessage}>
          로그인 후 기록 입력이 가능합니다.
        </div>
      )}

      {/* 로그인 후 버튼 */}
      {loggedIn && (
        <div className={styles.btnContainer}>
          <a href="addmember.html">
            <button type="button">멤버 등록</button>
          </a>
          <a href="addRecord.html">
            <button type="button">기록 입력</button>
          </a>
          <a href="recordList.html">
            <button type="button">전체 기록 조회</button>
          </a>
          <button type="button" onClick={handleLogout}>로그아웃</button>
        </div>
      )}

      {/* 항상 보이는 버튼 */}
      <div className={styles.alwaysVisibleBtnContainer}>
        <a href="periodList.html">
          <button type="button">기간별 기록 조회</button>
        </a>
        <a href="yakumanList.html">
          <button type="button">역만 기록 조회</button>
        </a>
        <a href="relativeRecord.html">
          <button type="button">상대 전적 조회</button>
        </a>
      </div>

      {/* 공지사항 테이블 */}
      <div className={styles.tableInfo}>
        <h2>룰 설명</h2>
        <table className={styles.noticeTable}>
          <thead>
            <tr>
              <th style={{ width: '70px' }}>번호</th>
              <th>제목</th>
              <th style={{ width: '100px' }}>날짜</th>
            </tr>
          </thead>
          <tbody>
            {notices.length === 0 ? (
              <tr>
                <td colSpan={3}>등록된 공지글이 없습니다.</td>
              </tr>
            ) : (
              notices.map((notice) => (
                <tr
                  key={notice.id}
                  onClick={() => showNoticeModal(notice)}
                  style={{ cursor: 'pointer' }}
                >
                  <td>{notice.id}</td>
                  <td className={styles.nicknameCell}>{notice.title}</td>
                  <td>{notice.date}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* 공지글 모달 */}
      {modalVisible && selectedNotice && (
        <div
          className={styles.modal}
          onClick={hideNoticeModal}
        >
          <div
            className={styles.modalContent}
            onClick={(e) => e.stopPropagation()}
          >
            <h4 className={styles.modalTitle}>{selectedNotice.title}</h4>
            <div className={styles.modalDate}>{selectedNotice.date}</div>
            <div className={styles.modalBody}>{selectedNotice.content}</div>
            <button
              className={styles.modalCloseBtn}
              onClick={hideNoticeModal}
            >
              닫기
            </button>
          </div>
        </div>
      )}

      {/* 순위 테이블 */}
      <div className={styles.rankingTable}>
        <h2>대탁 기록 & 순위</h2>
        <div className={styles.rankingTableWrapper}>
          <table className={styles.rankingTableBody}>
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
                  <td colSpan={10}>기록이 없습니다.</td>
                </tr>
              ) : (
                stats.map((stat, idx) => (
                  <tr key={stat.nickname}>
                    <td>{idx + 1}</td>
                    <td>
                      <a
                        href={`detail.html?nickname=${encodeURIComponent(stat.nickname)}`}
                        className={styles.detailLink}
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
      </div>

      {/* Footer */}
      <footer className={styles.footer}>
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
