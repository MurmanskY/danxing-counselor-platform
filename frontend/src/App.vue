<template>
  <div>
    <div v-if="state.loading" class="loading">
      <span class="brand-mark">旦</span>
      <p>正在打开旦行…</p>
    </div>
    <div v-else-if="state.error" class="loading">
      <h2>暂时无法连接服务</h2>
      <p>{{ state.error }}</p>
      <button class="primary" @click="boot">重试</button>
    </div>
    <main v-else-if="!state.user" class="login-page">
      <div class="login-art">
        <div class="brand">
          <span class="brand-mark">旦</span>
          <div>旦行<small>辅导员职业发展数据平台</small></div>
        </div>
        <div>
          <p class="eyebrow">DANXING · GROW TOGETHER</p>
          <h1>以初心为起点<br />让成长有方向</h1>
          <p>连接育人经验，记录每一步专业成长。</p>
        </div>
        <img src="/danxing-campus-v3.png" alt="校园建筑与绿树插画" />
      </div>
      <form class="login-form" @submit.prevent="signIn">
        <p class="eyebrow">WELCOME BACK</p>
        <h2>欢迎回到旦行无妨</h2>
        <p class="muted">登录你的职业成长工作台</p>
        <label
          >账号<input
            v-model="username"
            autocomplete="username"
            required
            placeholder="请输入账号" /></label
        ><label
          >密码<input
            v-model="password"
            type="password"
            autocomplete="current-password"
            required
            placeholder="请输入密码"
        /></label>
        <p v-if="loginError" class="error" role="alert">{{ loginError }}</p>
        <button class="primary wide" :disabled="signing">
          {{ signing ? "正在登录…" : "登录平台 →" }}</button
        ><small>首次使用请使用启动配置中的管理员账号。</small>
      </form>
    </main>
    <template v-else
      ><header
        class="site-header"
        :class="{ 'manager-theme': state.user?.role === 'COUNSELOR_ADMIN' }"
      >
        <router-link to="/" class="brand"
          ><span class="brand-mark">旦</span>
          <div>旦行<small>辅导员职业发展数据平台</small></div></router-link
        >
        <nav class="main-nav">
          <router-link v-for="n in nav" :key="n.path" :to="n.path" exact>{{
            n.label
          }}</router-link>
        </nav>
        <div
          class="user-menu"
          @mouseenter="cancelMenuClose"
          @mouseleave="scheduleMenuClose"
        >
          <button class="user-trigger" @click="menu = !menu">
            <img
              v-if="avatarUrl(state.user.avatarFileId)"
              class="avatar avatar-img"
              :src="avatarUrl(state.user.avatarFileId)"
              alt="用户头像"
            /><span v-else class="avatar">{{
              state.user.displayName.slice(0, 1)
            }}</span
            ><span>{{ state.user.displayName }}</span
            ><span>⌄</span>
          </button>
          <div v-if="menu" class="dropdown">
            <router-link to="/settings" @click.native="menu = false"
              >账号设置</router-link
            >
            <router-link to="/manage" @click.native="menu = false"
              >进入管理工作台</router-link
            ><router-link to="/manage/favorites" @click.native="menu = false"
              >我的收藏</router-link
            ><button @click="run(() => logout())">退出登录</button>
          </div>
        </div>
      </header>
      <div :class="{ workspace: managed }">
        <aside v-if="managed" class="sidebar">
          <p class="eyebrow">WORKSPACE</p>
          <h3>我的工作台</h3>
          <router-link v-for="n in manageNav" :key="n.path" :to="n.path" exact
            >{{ n.label }} <span>›</span></router-link
          ><router-link to="/" class="back-link">← 返回平台首页</router-link>
        </aside>
        <main :class="managed ? 'workspace-content' : 'page'">
          <router-view :key="$route.path" />
        </main>
      </div>
      <footer class="site-footer">
        <div><strong>旦行</strong><span>伴你走好育人路上的每一步</span></div>
        <small>开发团队：王囡、杨娅雯、袁懿、王骏飞</small>
      </footer>
      <nav class="mobile-nav">
        <router-link v-for="n in nav" :key="n.path" :to="n.path" exact
          ><span>{{ n.symbol }}</span
          >{{ n.label }}</router-link
        >
      </nav>
      <GlobalModals />
    </template>
    <div v-if="state.toast" class="toast" role="status">{{ state.toast }}</div>
  </div>
</template>
<script>
import { state, boot, login, logout, run, avatarUrl } from "./store";
import GlobalModals from "./components/GlobalModals.vue";
export default {
  components: { GlobalModals },
  data: () => ({
    state,
    username: "",
    password: "",
    signing: false,
    loginError: "",
    menu: false,
    menuTimer: null,
  }),
  computed: {
    nav() {
      const items = [
        { path: "/", label: "首页", symbol: "⌂" },
        { path: "/routes", label: "成长路线", symbol: "↗" },
        { path: "/honors", label: "荣誉殿堂", symbol: "☆" },
        { path: "/calendar", label: "发展日历", symbol: "▦" },
        { path: "/resources", label: "资源中心", symbol: "▤" },
      ];
      if (state.user?.role === "COUNSELOR_ADMIN")
        return items.filter((n) => n.path !== "/routes");
      return items;
    },
    managed() {
      return this.$route.path.startsWith("/manage");
    },
    manageNav() {
      let n = [
        ["", "工作概览"],
        ["people", "人员画像"],
        ["opportunities", "发展机会"],
        ["plans", "我的计划"],
        ["studios", "工作室"],
        ["favorites", "我的收藏"],
        ["files", "我的附件"],
        ["data", "数据治理"],
      ];
      if (state.user.permissions?.includes("MANAGE_USERS"))
        n.push(["users", "账号管理"]);
      if (state.user.permissions?.includes("VIEW_AUDIT"))
        n.push(["audit", "操作记录"]);
      return n.map(([path, label]) => ({
        path: "/manage" + (path ? "/" + path : ""),
        label,
      }));
    },
  },
  created() {
    boot();
  },
  methods: {
    boot,
    logout,
    run,
    avatarUrl,
    scheduleMenuClose() {
      this.cancelMenuClose();
      this.menuTimer = setTimeout(() => {
        this.menu = false;
      }, 250);
    },
    cancelMenuClose() {
      if (this.menuTimer) {
        clearTimeout(this.menuTimer);
        this.menuTimer = null;
      }
    },
    async signIn() {
      this.signing = true;
      this.loginError = "";
      try {
        await login(this.username, this.password);
        this.password = "";
      } catch (e) {
        this.loginError = e.message;
      } finally {
        this.signing = false;
      }
    },
  },
};
</script>
