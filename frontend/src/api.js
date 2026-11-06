let csrf = null;
export async function refreshCsrf() {
  const r = await fetch("/api/auth/csrf", { credentials: "same-origin" });
  if (!r.ok) throw new Error("服务尚未就绪，请稍后重试");
  csrf = await r.json();
}
export async function api(path, method = "GET", data) {
  const headers = {};
  if (method !== "GET") {
    if (!csrf) await refreshCsrf();
    headers[csrf.headerName] = csrf.token;
  }
  let body;
  if (data instanceof FormData || data instanceof URLSearchParams) body = data;
  else if (data !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(data);
  }
  const r = await fetch("/api" + path, {
    method,
    body,
    headers,
    credentials: "same-origin",
  });
  const content = await r.text();
  let parsed;
  try {
    parsed = content ? JSON.parse(content) : null;
  } catch {
    parsed = null;
  }
  if (!r.ok) {
    const error = new Error(parsed?.message || "请求失败，请稍后重试");
    error.status = r.status;
    throw error;
  }
  return parsed;
}
