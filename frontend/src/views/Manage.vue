<template>
  <div>
    <template v-if="section === 'overview'"
      ><header class="page-heading row-heading">
        <div>
          <p class="eyebrow">YOUR GROWTH WORKSPACE</p>
          <h1>{{ state.user.displayName }}，你好</h1>
          <p>从这里管理资料与计划，记录每一份成长积累。</p>
        </div>
        <button class="primary" @click="newPlan()">＋ 新建计划</button>
      </header>
      <div class="stats-grid">
        <div class="stat-box" v-for="m in stats" :key="m.label">
          <small>{{ m.label }}</small
          ><strong>{{ m.value }}</strong>
        </div>
      </div>
      <div class="home-columns">
        <section class="panel">
          <div class="section-head">
            <h2>成长进度</h2>
            <router-link to="/manage/plans">全部计划 ↗</router-link>
          </div>
          <div v-if="!state.plans.length" class="welcome-task">
            <div>
              <h3>为自己确定一个可实现的目标</h3>
              <p>从成长路线出发，创建你的第一份计划。</p>
              <router-link class="text-button" to="/routes"
                >探索成长路线 →</router-link
              >
            </div>
          </div>
          <router-link
            v-for="v in state.plans.slice(0, 4)"
            :key="v.plan.id"
            class="overview-plan"
            to="/manage/plans"
            ><div>
              <strong>{{ v.plan.title }}</strong
              ><span
                >{{ v.tasks.filter((t) => t.done).length }} /
                {{ v.tasks.length }} 项</span
              >
            </div>
            <div class="progress">
              <i
                :style="{
                  width:
                    (v.tasks.filter((t) => t.done).length / v.tasks.length) *
                      100 +
                    '%',
                }"
              ></i></div
          ></router-link>
        </section>
        <section class="panel">
          <div class="section-head">
            <h2>数据准备状态</h2>
            <router-link to="/manage/data">查看详情 ↗</router-link>
          </div>
          <div class="status-line">
            <span>原始资料</span
            ><strong>{{ state.records.length }} 条已导入</strong>
          </div>
          <div class="status-line">
            <span>详细人员画像</span><strong>4 份</strong>
          </div>
          <div class="status-line">
            <span>历史人员工号匹配</span><span class="tag">148 条待核验</span>
          </div>
          <div class="status-line">
            <span>平台补充核验</span><strong>{{ verified }} 条已核验</strong>
          </div>
          <p class="notice">原始资料不自动覆盖，核验结论单独留痕。</p>
        </section>
      </div></template
    >
    <template v-else-if="section === 'data'"
      ><header class="page-heading">
        <p class="eyebrow">DATA & GOVERNANCE</p>
        <h1>数据治理</h1>
        <p>保留来源、核验事实，让每一条资料都有据可查。</p>
      </header>
      <div class="notice">
        本版迁移 567
        条资料。eHall、统一身份认证及正式通知接口尚未接入；历史记录不代表当前人员在岗情况或开放申报项目。
      </div>
      <div class="section">
        <h2>数据使用规则</h2>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th v-for="key in keys(state.rules)" :key="key">{{ key }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in state.rules" :key="i">
                <td v-for="key in keys(state.rules)" :key="key">
                  {{ row[key] }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="section">
        <h2>上线准备清单</h2>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th v-for="key in keys(state.checklist)" :key="key">
                  {{ key }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in state.checklist" :key="i">
                <td v-for="key in keys(state.checklist)" :key="key">
                  {{ row[key] }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div></template
    >
    <template v-else-if="section === 'users' && canManageUsers"
      ><header class="page-heading row-heading">
        <div>
          <p class="eyebrow">ACCOUNTS & ACCESS</p>
          <h1>账号管理</h1>
          <p>维护辅导员账号、角色及关联画像。</p>
        </div>
        <button class="primary" @click="adding = true">＋ 新建账号</button>
      </header>
      <p v-if="loading">正在载入账号…</p>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>账号</th>
              <th>头像</th>
              <th>显示名称</th>
              <th>角色</th>
              <th>关联画像</th>
              <th>创建日期</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>{{ u.username }}</td>
              <td>
                <div class="account-avatar-control">
                  <img
                    v-if="avatarUrl(u.avatarFileId)"
                    class="avatar account-avatar avatar-img"
                    :src="avatarUrl(u.avatarFileId)"
                    :alt="u.displayName + ' 的头像'"
                  /><span v-else class="avatar account-avatar">{{
                    u.displayName.slice(0, 1)
                  }}</span>
                  <label class="button avatar-upload"
                    >{{ savingAvatarId === u.id ? "上传中…" : "上传头像" }}<input
                      type="file"
                      accept="image/png,image/jpeg"
                      :disabled="savingAvatarId === u.id"
                      hidden
                      @change="pickUserAvatar($event, u)"
                  /></label>
                  <button
                    v-if="u.avatarFileId"
                    :disabled="savingAvatarId === u.id"
                    @click="resetUserAvatar(u)"
                  >
                    恢复默认
                  </button>
                </div>
              </td>
              <td>{{ u.displayName }}</td>
              <td>
                <span class="tag">{{ u.roleName || u.role }}</span>
              </td>
              <td>{{ profileTitle(u.profileId) }}</td>
              <td>{{ u.createdAt.slice(0, 10) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <Modal v-if="adding" title="新建辅导员账号" @close="adding = false"
        ><form class="modal-body" @submit.prevent="createUser">
          <label
            >账号<input
              v-model="userForm.username"
              pattern="[A-Za-z0-9_.-]{3,60}"
              required
              autocomplete="off" /></label
          ><label
            >显示名称<input
              v-model="userForm.displayName"
              maxlength="80"
              required /></label
          ><label
            >初始密码<input
              v-model="userForm.password"
              type="password"
              minlength="12"
              maxlength="72"
              required
              autocomplete="new-password" /></label
          ><label
            >角色<select v-model="userForm.role" @change="roleChanged">
              <option v-for="role in roles" :key="role.code" :value="role.code">
                {{ role.name }}
              </option>
            </select></label
          ><label
            >关联画像<select v-model="userForm.profileId" required>
              <option disabled value="">请选择匹配的辅导员画像</option>
              <option
                v-for="profile in profilesForRole"
                :key="profile.profileId"
                :value="profile.profileId"
              >
                {{ profile.title }} · {{ profile.department }}
              </option>
            </select></label
          >
          <div class="form-actions">
            <button class="primary" :disabled="saving">创建账号</button>
          </div>
        </form></Modal
      ></template
    >
    <template v-else-if="section === 'audit' && canViewAudit"
      ><header class="page-heading">
        <p class="eyebrow">AUDIT TRAIL</p>
        <h1>操作记录</h1>
        <p>展示最近 100 条业务操作，便于追溯资料维护与个人变更。</p>
      </header>
      <p v-if="loading">正在载入记录…</p>
      <div v-if="!logs.length && !loading" class="empty">暂无操作记录</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>操作人</th>
              <th>操作</th>
              <th>对象</th>
              <th>说明</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="l in logs" :key="l.id">
              <td>{{ new Date(l.createdAt).toLocaleString("zh-CN") }}</td>
              <td>{{ userName(l.actorId) }}</td>
              <td>{{ l.action }}</td>
              <td>{{ l.targetId }}</td>
              <td>{{ l.detail || "—" }}</td>
            </tr>
          </tbody>
        </table>
      </div></template
    >
    <div v-else class="empty">
      <h2>此页面不可用</h2>
      <router-link to="/manage">返回工作概览</router-link>
    </div>
  </div>
</template>
<script>
import { state, run, newPlan, notify, avatarUrl } from "../store";
import { api } from "../api";
import Modal from "../components/Modal.vue";
export default {
  components: { Modal },
  props: ["section"],
  data: () => ({
    state,
    users: [],
    roles: [],
    counselorProfiles: [],
    logs: [],
    loading: false,
    adding: false,
    saving: false,
    savingAvatarId: "",
    userForm: {
      username: "",
      displayName: "",
      password: "",
      role: "COUNSELOR",
      profileId: "P002",
    },
  }),
  computed: {
    canManageUsers() {
      return state.user.permissions?.includes("MANAGE_USERS");
    },
    canViewAudit() {
      return state.user.permissions?.includes("VIEW_AUDIT");
    },
    profilesForRole() {
      return this.counselorProfiles.filter(
        (profile) => profile.roleCode === this.userForm.role,
      );
    },
    verified() {
      return state.extensions.filter((e) => e.reviewStatus === "已核验").length;
    },
    stats() {
      return [
        { label: "我的发展计划", value: state.plans.length },
        {
          label: "已完成计划",
          value: state.plans.filter((v) => v.plan.status === "已完成").length,
        },
        { label: "我的收藏", value: state.favorites.length },
        { label: "个人附件", value: state.files.length },
      ];
    },
  },
  async created() {
    if (
      (this.section === "users" && this.canManageUsers) ||
      (this.section === "audit" && this.canViewAudit)
    ) {
      this.loading = true;
      await run(async () => {
        if (this.section === "users") {
          [this.users, this.roles, this.counselorProfiles] = await Promise.all([
            api("/admin/users"),
            api("/admin/roles"),
            api("/admin/counselor-profiles"),
          ]);
        } else {
          this.logs = await api("/admin/audit");
          if (this.canManageUsers) this.users = await api("/admin/users");
        }
      });
      this.loading = false;
    }
  },
  methods: {
    newPlan,
    avatarUrl,
    keys(rows) {
      return Object.keys(rows[0] || {});
    },
    userName(id) {
      return this.users.find((u) => u.id === id)?.displayName || id;
    },
    profileTitle(id) {
      return this.counselorProfiles.find((profile) => profile.profileId === id)
        ?.title || id || "未关联";
    },
    roleChanged() {
      if (
        !this.profilesForRole.some(
          (profile) => profile.profileId === this.userForm.profileId,
        )
      ) {
        this.userForm.profileId = this.profilesForRole[0]?.profileId || "";
      }
    },
    async createUser() {
      this.saving = true;
      await run(async () => {
        await api("/admin/users", "POST", this.userForm);
        [this.users, this.counselorProfiles] = await Promise.all([
          api("/admin/users"),
          api("/admin/counselor-profiles"),
        ]);
        this.adding = false;
        this.userForm = {
          username: "",
          displayName: "",
          password: "",
          role: "COUNSELOR",
          profileId:
            this.counselorProfiles.find(
              (profile) => profile.roleCode === "COUNSELOR",
            )?.profileId || "",
        };
        notify("账号已创建");
      });
      this.saving = false;
    },
    async pickUserAvatar(event, user) {
      const file = event.target.files?.[0];
      event.target.value = "";
      if (!file) return;
      this.savingAvatarId = user.id;
      await run(async () => {
        const data = new FormData();
        data.append("file", file);
        const updated = await api(
          `/admin/users/${encodeURIComponent(user.id)}/avatar`,
          "POST",
          data,
        );
        this.$set(user, "avatarFileId", updated.avatarFileId);
        if (state.user.id === user.id) state.user = updated;
        state.profileAvatars = await api("/profiles/avatars");
        notify("用户头像已更新");
      });
      this.savingAvatarId = "";
    },
    async resetUserAvatar(user) {
      this.savingAvatarId = user.id;
      await run(async () => {
        const updated = await api(
          `/admin/users/${encodeURIComponent(user.id)}/avatar`,
          "DELETE",
        );
        this.$set(user, "avatarFileId", updated.avatarFileId);
        if (state.user.id === user.id) state.user = updated;
        state.profileAvatars = await api("/profiles/avatars");
        notify("用户头像已恢复默认");
      });
      this.savingAvatarId = "";
    },
  },
};
</script>
