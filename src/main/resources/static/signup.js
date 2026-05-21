const $ = (selector) => document.querySelector(selector);

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

$("#signupForm").addEventListener("submit", async (event) => {
  event.preventDefault();

  const name = $("#name").value.trim();
  const email = $("#email").value.trim();
  const rawPassword = $("#rawPassword").value;

  if (!name || !email || !rawPassword) {
    setMessage("회원가입 정보를 모두 입력해 주세요.");
    return;
  }

  try {
    await api("/members", {
      method: "POST",
      body: JSON.stringify({ name, email, rawPassword })
    });
    setMessage("회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.");
    setTimeout(() => {
      window.location.href = "/login.html";
    }, 1200);
  } catch (error) {
    setMessage(error.message);
  }
});
