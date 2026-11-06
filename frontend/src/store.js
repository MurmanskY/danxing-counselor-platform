import Vue from "vue";
import { api, refreshCsrf } from "./api";
export const state = Vue.observable({
  user: null,
  loading: true,
  error: "",
  toast: "",
  records: [],
  extensions: [],
  rules: [],
  checklist: [],
  plans: [],
  favorites: [],
  calendar: [],
  calendarEvents: [],
  dashboard: { profileId: "", managedProfileIds: [] },
  files: [],
  profileAvatars: {},
  detail: null,
  planDraft: null,
  calendarDraft: null,
  review: null,
});
let timer;
export function notify(text) {
  state.toast = text;
  clearTimeout(timer);
  timer = setTimeout(() => (state.toast = ""), 4000);
}
export async function run(fn) {
  try {
    return await fn();
  } catch (e) {
    notify(e.message);
    return null;
  }
}
export async function refreshCatalog() {
  const c = await api("/catalog");
  Object.assign(state, c);
}
export async function refreshPersonal() {
  const [plans, favorites, calendar, calendarEvents, files] = await Promise.all([
    api("/plans"),
    api("/favorites"),
    api("/calendar"),
    api("/calendar/events"),
    api("/files"),
  ]);
  Object.assign(state, { plans, favorites, calendar, calendarEvents, files });
}
export function hasPermission(permission) {
  return Boolean(state.user?.permissions?.includes(permission));
}
export async function boot() {
  state.loading = true;
  state.error = "";
  try {
    await refreshCsrf();
    state.user = await api("/auth/me");
    await Promise.all([
      refreshCatalog(),
      refreshPersonal(),
      api("/profiles/avatars").then((avatars) => {
        state.profileAvatars = avatars || {};
      }),
      api("/home/dashboard").then((dashboard) => {
        state.dashboard = dashboard;
      }),
    ]);
  } catch (e) {
    if (e.status === 401) state.user = null;
    else state.error = e.message;
  } finally {
    state.loading = false;
  }
}
export async function login(username, password) {
  await api("/auth/login", "POST", new URLSearchParams({ username, password }));
  await refreshCsrf();
  await boot();
}
export async function logout() {
  await api("/auth/logout", "POST");
  Object.assign(state, {
    user: null,
    records: [],
    plans: [],
    favorites: [],
    calendar: [],
    calendarEvents: [],
    dashboard: { profileId: "", managedProfileIds: [] },
    files: [],
    profileAvatars: {},
    detail: null,
    planDraft: null,
    calendarDraft: null,
    review: null,
  });
  await refreshCsrf();
}
export function avatarUrl(fileId) {
  return fileId ? "/api/files/" + fileId + "/avatar" : "";
}
export function profileAvatarUrl(profileId) {
  return avatarUrl(state.profileAvatars[profileId]);
}
export async function uploadAvatar(file) {
  const data = new FormData();
  data.append("file", file);
  state.user = await api("/me/avatar", "POST", data);
  if (state.user.profileId)
    Vue.set(state.profileAvatars, state.user.profileId, state.user.avatarFileId);
  notify("头像已更新");
}
export async function removeAvatar() {
  state.user = await api("/me/avatar", "DELETE");
  if (state.user.profileId)
    Vue.set(state.profileAvatars, state.user.profileId, state.user.avatarFileId);
  notify("已恢复默认头像");
}
export async function favorite(id) {
  const active = !state.favorites.includes(id);
  await api("/favorites/" + id, "PUT", { active });
  state.favorites = active
    ? [...state.favorites, id]
    : state.favorites.filter((x) => x !== id);
  notify(active ? "已加入我的收藏" : "已取消收藏");
}
export function record(id) {
  return state.records.find((r) => r.id === id);
}
export function extension(id) {
  return (
    state.extensions.find((e) => e.recordId === id) || {
      reviewStatus: "待核验",
      keywords: "",
      philosophy: "",
      note: "",
    }
  );
}
export const kinds = {
  person: "人员画像",
  honor: "个人荣誉",
  collective: "集体荣誉",
  outcome: "育人成果",
  opportunity: "发展机会",
  studio: "工作室",
};
export function showRecord(id) {
  state.detail = typeof id === "object" ? id : record(id);
}
export function newPlan(draft = {}) {
  state.planDraft = {
    title: "",
    domain: "专业能力提升",
    deadline: "",
    description: "",
    personId: "",
    opportunityId: "",
    tasks: [
      "梳理个人积累与目标",
      "核验项目要求与材料",
      "完成准备并开展阶段复盘",
    ],
    ...draft,
  };
}
export function newCalendar(draft = {}) {
  const today = new Date().toLocaleDateString("sv-SE");
  state.calendarDraft = {
    title: "",
    startDate: today,
    endDate: today,
    category: "个人准备",
    note: "",
    recordId: "",
    ...draft,
  };
}
