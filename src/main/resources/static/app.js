const $ = (selector) => document.querySelector(selector);
const AUTH_STORAGE_KEY = "roomescapeAuth";

const state = {
  themes: [],
  availableTimes: []
};

async function api(path, options = {}) {
  const { headers = {}, ...restOptions } = options;
  const mergedHeaders = {
    "Content-Type": "application/json",
    ...headers
  };

  const response = await fetch(path, {
    headers: mergedHeaders,
    ...restOptions
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "요청 처리에 실패했습니다.");
  }

  if (response.status === 204) return null;
  const text = await response.text();
  if (!text) return null;
  return JSON.parse(text);
}

function setMessage(message) {
  $("#message").textContent = message;
}

function getAuth() {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    return null;
  }
}

function setAuth(auth) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
  renderAuthStatus();
}

function clearAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY);
  renderAuthStatus();
}

function buildAuthHeader() {
  const auth = getAuth();
  const accessToken = auth?.accessToken?.trim();
  if (!accessToken) {
    throw new Error("로그인이 필요합니다.");
  }
  return `Bearer ${accessToken}`;
}

function getMemberIdFromToken() {
  const auth = getAuth();
  if (!auth?.accessToken) {
    throw new Error("로그인이 필요합니다.");
  }

  const tokenParts = auth.accessToken.split(".");
  if (tokenParts.length < 2) {
    throw new Error("유효하지 않은 토큰입니다.");
  }

  try {
    const base64Url = tokenParts[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
    const payload = JSON.parse(atob(padded));
    const memberId = Number(payload.sub);
    if (!Number.isInteger(memberId)) {
      throw new Error("유효하지 않은 토큰입니다.");
    }
    return memberId;
  } catch {
    throw new Error("유효하지 않은 토큰입니다.");
  }
}

function renderAuthStatus() {
  const auth = getAuth();
  if (!auth?.accessToken) {
    $("#authStatus").textContent = "로그인이 필요합니다.";
    return;
  }
  $("#authStatus").textContent = `${auth.email} 계정으로 로그인됨`;
}

async function authApi(path, options = {}) {
  const authHeader = buildAuthHeader();
  const headers = {
    ...(options.headers || {}),
    Authorization: authHeader
  };
  return api(path, { ...options, headers });
}

function renderThemeOptions() {
  const select = $("#createThemeId");
  select.innerHTML = '<option value="">테마 선택</option>';

  state.themes.forEach((theme) => {
    const option = document.createElement("option");
    option.value = theme.id;
    option.textContent = theme.name;
    select.appendChild(option);
  });
}

function renderAvailableTimes() {
  const root = $("#availableTimes");
  root.innerHTML = "";

  if (state.availableTimes.length === 0) {
    root.textContent = "예약 가능한 시간이 없습니다.";
    return;
  }

  state.availableTimes.forEach((time) => {
    const button = document.createElement("button");
    button.className = "chip";
    button.type = "button";
    button.dataset.timeId = time.id;
    button.textContent = `${time.startAt} 예약`;
    root.appendChild(button);
  });
}

function renderReservations(reservations) {
  const root = $("#reservations");
  if (!reservations.length) {
    root.textContent = "아직 예약이 없습니다.";
    return;
  }

  root.innerHTML = "";

  reservations.forEach((reservation) => {
    const row = document.createElement("div");
    row.className = "reservation-row";
    row.innerHTML = `
      <span class="reservation-text">${reservation.id}. [${reservation.theme?.name ?? "테마 없음"}] ${reservation.date} ${reservation.time.startAt} - ${reservation.member?.name ?? "회원"}</span>
      <div class="reservation-actions">
        <button class="ghost reservation-update" data-id="${reservation.id}" data-theme-id="${reservation.theme?.id ?? ""}" type="button">변경</button>
        <button class="danger reservation-delete" data-id="${reservation.id}" type="button">삭제</button>
      </div>
    `;
    root.appendChild(row);
  });
}

function renderPopularThemes(popularThemes) {
  const list = $("#popularThemes");
  list.innerHTML = "";
  if (!popularThemes.length) {
    const li = document.createElement("li");
    li.textContent = "최근 1주 예약 데이터가 없습니다.";
    list.appendChild(li);
    return;
  }

  popularThemes.forEach((theme) => {
    const li = document.createElement("li");
    li.textContent = `${theme.name} - ${theme.description}`;
    list.appendChild(li);
  });
}

async function loadThemes() {
  state.themes = await api("/themes");
  renderThemeOptions();
}

async function loadReservations() {
  const reservations = await authApi("/members/me/reservations");
  renderReservations(reservations);
}

async function loadPopularThemes() {
  const popular = await api("/themes?popular=true&period=7&limit=10");
  renderPopularThemes(popular);
}

async function loadAvailableTimes() {
  const date = $("#createDate").value;
  const themeId = $("#createThemeId").value;

  if (!date || !themeId) {
    setMessage("날짜와 테마를 먼저 선택해 주세요.");
    return;
  }

  state.availableTimes = await api(`/times/available-times?date=${date}&themeId=${themeId}`);
  renderAvailableTimes();
}

$("#loadTimes").addEventListener("click", async () => {
  try {
    await loadAvailableTimes();
    setMessage("예약 가능한 시간을 조회했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#loadReservations").addEventListener("click", async () => {
  try {
    await loadReservations();
    setMessage("예약 내역을 조회했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#availableTimes").addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-time-id]");
  if (!button) return;

  const date = $("#createDate").value;
  const themeId = $("#createThemeId").value;

  if (!date || !themeId) {
    setMessage("날짜와 테마를 모두 입력해 주세요.");
    return;
  }

  try {
    const memberId = getMemberIdFromToken();
    await authApi("/members/me/reservations", {
      method: "POST",
      body: JSON.stringify({
        memberId,
        date,
        timeId: Number(button.dataset.timeId),
        themeId: Number(themeId)
      })
    });

    await loadAvailableTimes();
    await loadPopularThemes();
    await loadReservations();
    $("#reservationSuccess").textContent = "예약 성공: 예약이 정상적으로 생성되었습니다.";
    setMessage("예약이 정상적으로 완료되었습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#loadPopular").addEventListener("click", async () => {
  try {
    await loadPopularThemes();
    setMessage("인기 테마를 갱신했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#reservations").addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-id]");
  if (!button) return;

  const reservationId = button.dataset.id;
  if (button.classList.contains("reservation-update")) {
    const themeId = button.dataset.themeId;
    window.location.href = `/reservation-update.html?id=${encodeURIComponent(reservationId)}&themeId=${encodeURIComponent(themeId)}`;
    return;
  }

  window.location.href = `/reservation-cancel.html?id=${encodeURIComponent(reservationId)}`;
});

async function init() {
  try {
    renderAuthStatus();
    await loadThemes();
    await loadPopularThemes();
    setMessage("초기 데이터 로딩 완료");
  } catch (error) {
    setMessage(error.message);
  }
}

init();
