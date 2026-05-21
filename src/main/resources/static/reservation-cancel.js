const $ = (selector) => document.querySelector(selector);

const query = new URLSearchParams(window.location.search);
const reservationId = query.get("id");
const AUTH_STORAGE_KEY = "roomescapeAuth";

function setMessage(message) {
  $("#message").textContent = message;
}

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
  return response.json();
}

function getAuthHeader() {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY);
  if (!raw) {
    throw new Error("로그인이 필요합니다.");
  }
  const auth = JSON.parse(raw);
  if (!auth?.accessToken) {
    throw new Error("로그인이 필요합니다.");
  }
  return `${auth.tokenType || "Bearer"} ${auth.accessToken}`;
}

async function authApi(path, options = {}) {
  const headers = {
    ...(options.headers || {}),
    Authorization: getAuthHeader()
  };
  return api(path, { ...options, headers });
}

function initPage() {
  if (!reservationId) {
    $("#cancelReservation").disabled = true;
    $("#reservationInfo").textContent = "예약 번호가 없어 취소를 진행할 수 없습니다.";
    setMessage("잘못된 접근입니다. 사용자 예약 페이지에서 다시 시도해 주세요.");
    return false;
  }

  $("#reservationInfo").textContent = `예약 번호 #${reservationId} 를 취소합니다.`;
  try {
    getAuthHeader();
    return true;
  } catch (error) {
    $("#cancelReservation").disabled = true;
    setMessage(error.message);
    return false;
  }
}

$("#cancelForm").addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!reservationId) return;

  try {
    await authApi(`/members/me/reservations/${reservationId}`, {
      method: "DELETE",
    });
    setMessage("예약이 취소되었습니다. 잠시 후 사용자 페이지로 이동합니다.");
    setTimeout(() => {
      window.location.href = "/index.html";
    }, 1200);
  } catch (error) {
    setMessage(error.message);
  }
});

initPage();
