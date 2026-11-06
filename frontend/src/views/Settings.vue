<template>
  <div class="settings-page">
    <header class="page-heading">
      <p class="eyebrow">ACCOUNT SETTINGS</p>
      <h1>账号设置</h1>
      <p>管理你的登录账号与个人头像，头像会同步展示在首页画像与导航栏中。</p>
    </header>
    <section class="panel settings-panel">
      <div class="settings-avatar-block">
        <img
          v-if="avatarUrl(state.user.avatarFileId)"
          class="settings-avatar avatar-img"
          :src="avatarUrl(state.user.avatarFileId)"
          alt="当前头像"
        />
        <div v-else class="settings-avatar settings-avatar-fallback">
          {{ state.user.displayName.slice(0, 1) }}
        </div>
        <div class="settings-avatar-actions">
          <h2>个人头像</h2>
          <p class="muted">
            支持 PNG / JPG 格式，建议为正方形证件照或形象照。
          </p>
          <div class="button-row">
            <label class="primary settings-upload"
              >上传头像<input
                type="file"
                accept="image/png,image/jpeg"
                hidden
                @change="pickAvatar"
            /></label>
            <button
              v-if="state.user.avatarFileId"
              @click="run(() => removeAvatar())"
            >
              恢复默认头像
            </button>
          </div>
          <p v-if="busy" class="muted">正在上传…</p>
        </div>
      </div>
      <dl class="facts settings-facts">
        <dt>姓名</dt>
        <dd>{{ state.user.displayName }}</dd>
        <dt>登录账号</dt>
        <dd>{{ state.user.username }}</dd>
        <dt>账号角色</dt>
        <dd>{{ roleLabel }}</dd>
      </dl>
    </section>
  </div>
</template>
<script>
import { state, run, avatarUrl, uploadAvatar, removeAvatar } from "../store";
export default {
  data: () => ({ state, busy: false }),
  computed: {
    roleLabel() {
      const role = state.user?.role;
      if (role === "COUNSELOR_ADMIN") return "管理员辅导员";
      if (role === "COUNSELOR") return "普通辅导员";
      return role || "未设置";
    },
  },
  methods: {
    run,
    avatarUrl,
    removeAvatar,
    async pickAvatar(event) {
      const file = event.target.files && event.target.files[0];
      event.target.value = "";
      if (!file) return;
      this.busy = true;
      try {
        await run(() => uploadAvatar(file));
      } finally {
        this.busy = false;
      }
    },
  },
};
</script>
