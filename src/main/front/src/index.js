import React from 'react';
import ReactDOM from 'react-dom/client';

// 환경 변수로 어떤 폴더를 빌드할지 선택
const FOLDER = process.env.REACT_APP_FOLDER || 'sub';

let App;

if (FOLDER === 'sub') {
  App = require('./components/SubPage/SubPage').default;
} else if (FOLDER === 'wall') {
  App = require('./components/WallRecordSystem/WallRecordSystem').default;
} else if (FOLDER === 'sub2') {
  App = require('./components/SubPage2/SubPage2').default;
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
