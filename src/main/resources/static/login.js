const $ = (selector) => document.querySelector(selector);
const AUTH_STORAGE_KEY = "roomescapeAuth";

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

function getAuthHeader() {
  const auth = getAuth();
  const accessToken = auth?.accessToken?.trim();
  if (!accessToken) {
    throw new Error("로그인이 필요합니다.");
  }
  return `Bearer ${accessToken}`;
}

function renderAuthStatus() {
  const auth = getAuth();
  if (!auth?.accessToken) {
    $("#authStatus").textContent = "로그인이 필요합니다.";
    return;
  }
  $("#authStatus").textContent = `${auth.email} 계정으로 로그인됨`;
}

$("#loginForm").addEventListener("submit", async (event) => {
  event.preventDefault();

  const email = $("#email").value.trim();
  const password = $("#password").value;

  if (!email || !password) {
    setMessage("로그인 정보를 모두 입력해 주세요.");
    return;
  }

  try {
    const loginResult = await api("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });

    setAuth({
      email,
      accessToken: loginResult.accessToken,
      tokenType: loginResult.tokenType,
      expiresIn: loginResult.expiresIn
    });

    setMessage("로그인이 완료되었습니다. 사용자 페이지로 이동합니다.");
    setTimeout(() => {
      window.location.href = "/index.html";
    }, 1200);
  } catch (error) {
    setMessage(error.message);
  }
});

$("#logoutButton").addEventListener("click", () => {
  clearAuth();
  setMessage("로그아웃되었습니다.");
});

$("#deleteMemberButton").addEventListener("click", async () => {
  try {
    await api("/members/me", {
      method: "DELETE",
      headers: {
        Authorization: getAuthHeader()
      }
    });
    clearAuth();
    setMessage("회원 탈퇴가 완료되었습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

renderAuthStatus();
