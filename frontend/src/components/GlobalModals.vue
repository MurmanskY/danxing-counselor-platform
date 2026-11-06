<template>
  <div>
    <Modal
      v-if="state.detail"
      :title="state.detail.title"
      @close="state.detail = null"
      ><div class="modal-body">
        <div class="tags">
          <span class="tag">{{ kinds[state.detail.kind] }}</span
          ><span class="tag neutral">{{
            extension(state.detail.id).reviewStatus
          }}</span
          ><span class="muted">资料编号 {{ state.detail.id }}</span>
        </div>
        <p class="notice">
          原始资料按来源保留。人员关联、历史项目的现行条件及公开范围仍需核验。
        </p>
        <div class="detail-actions">
          <button class="primary" @click="fromRecord">加入发展计划</button
          ><button @click="calendarRecord">添加个人日程</button
          ><button @click="run(() => favorite(state.detail.id))">
            {{
              state.favorites.includes(state.detail.id)
                ? "取消收藏"
                : "☆ 收藏资料"
            }}</button
          ><button @click="download">下载资料摘要</button>
        </div>
        <dl class="facts">
          <template v-for="(value, key) in state.detail.payload"
            ><dt :key="key + 'k'">{{ key }}</dt>
            <dd :key="key + 'v'">
              {{
                typeof value === "object"
                  ? JSON.stringify(value)
                  : value || "未提供"
              }}
            </dd></template
          >
        </dl>
        <section class="extension">
          <h3>平台补充信息</h3>
          <p>
            <strong>育人关键词：</strong
            >{{ extension(state.detail.id).keywords || "待补充" }}
          </p>
          <p>
            <strong>育人理念：</strong
            >{{ extension(state.detail.id).philosophy || "待补充" }}
          </p>
          <p>
            <strong>核验依据：</strong
            >{{ extension(state.detail.id).note || "尚未核验" }}
          </p>
          <button
            v-if="state.user.permissions?.includes('REVIEW_RECORDS')"
            @click="reviewRecord"
          >
            编辑补充与核验信息
          </button>
        </section>
        <p class="muted">来源：{{ state.detail.source }} · 原始字段只读</p>
      </div></Modal
    >
    <Modal
      v-if="state.planDraft"
      :title="state.planDraft.id ? '编辑发展计划' : '创建发展计划'"
      @close="state.planDraft = null"
      ><form class="modal-body form-grid" @submit.prevent="savePlan">
        <label class="full"
          >计划标题<input
            v-model="state.planDraft.title"
            maxlength="120"
            required /></label
        ><label v-if="!state.planDraft.id"
          >发展领域<input
            v-model="state.planDraft.domain"
            maxlength="60"
            required /></label
        ><label
          >目标日期<input
            v-model="state.planDraft.deadline"
            type="date" /></label
        ><template v-if="!state.planDraft.id"
          ><label
            >关联人员画像<select v-model="state.planDraft.personId">
              <option value="">暂不关联</option>
              <option v-for="p in detailedPeople" :key="p.id" :value="p.id">
                {{ p.title }} · {{ p.department }}
              </option>
            </select></label
          ><label
            >参考历史项目<select v-model="state.planDraft.opportunityId">
              <option value="">暂不关联</option>
              <option v-for="r in opportunities" :key="r.id" :value="r.id">
                {{ r.title }}
              </option>
            </select></label
          >
          <div class="full">
            <label>阶段任务（1–12 项）</label>
            <div
              v-for="(task, i) in state.planDraft.tasks"
              :key="i"
              class="task-input"
            >
              <input
                v-model="state.planDraft.tasks[i]"
                maxlength="180"
                required
                :aria-label="'任务 ' + (i + 1)"
              /><button
                type="button"
                :disabled="state.planDraft.tasks.length === 1"
                @click="state.planDraft.tasks.splice(i, 1)"
                aria-label="删除任务"
              >
                ×
              </button>
            </div>
            <button
              type="button"
              :disabled="state.planDraft.tasks.length >= 12"
              @click="state.planDraft.tasks.push('')"
            >
              ＋ 添加任务
            </button>
          </div></template
        ><label class="full"
          >计划说明<textarea
            v-model="state.planDraft.description"
            maxlength="2000"
            rows="3"
          ></textarea>
        </label>
        <div class="form-actions full">
          <button type="button" @click="state.planDraft = null">取消</button
          ><button class="primary" :disabled="busy">
            {{ busy ? "正在保存…" : "保存计划" }}
          </button>
        </div>
      </form></Modal
    >
    <Modal
      v-if="state.calendarDraft"
      :title="state.calendarDraft.id ? '编辑日程' : '添加个人日程'"
      @close="state.calendarDraft = null"
      ><form class="modal-body form-grid" @submit.prevent="saveCalendar">
        <label class="full"
          >日程标题<input
            v-model="state.calendarDraft.title"
            required
            maxlength="150" /></label
        ><label
          >开始日期<input
            v-model="state.calendarDraft.startDate"
            type="date"
            required /></label
        ><label
          >结束日期<input
            v-model="state.calendarDraft.endDate"
            type="date"
            required
            :min="state.calendarDraft.startDate" /></label
        ><label class="full"
          >类别<select v-model="state.calendarDraft.category">
            <option>个人准备</option>
            <option>申报安排（待核验）</option>
          </select></label
        ><label class="full"
          >备注<textarea
            v-model="state.calendarDraft.note"
            maxlength="1000"
            rows="3"
          ></textarea>
        </label>
        <p class="notice full">
          个人日程只对当前账号可见，不作为官方申报日历。
        </p>
        <div class="form-actions full">
          <button
            v-if="state.calendarDraft.id"
            type="button"
            class="danger"
            @click="deleteCalendar"
          >
            删除日程</button
          ><button class="primary" :disabled="busy">保存日程</button>
        </div>
      </form></Modal
    >
    <Modal
      v-if="state.review"
      title="资料补充与核验"
      @close="state.review = null"
      ><form class="modal-body" @submit.prevent="saveReview">
        <p class="notice">编辑平台补充字段，不覆盖原始资料。</p>
        <label
          >育人关键词<input
            v-model="state.review.keywords"
            maxlength="200" /></label
        ><label
          >育人理念<textarea
            v-model="state.review.philosophy"
            maxlength="1000"
            rows="3"
          ></textarea></label
        ><label
          >核验状态<select v-model="state.review.reviewStatus">
            <option>待核验</option>
            <option>已核验</option>
            <option>需修正</option>
          </select></label
        ><label
          >核验依据 / 备注<textarea
            v-model="state.review.note"
            maxlength="1000"
            rows="3"
            :required="state.review.reviewStatus === '已核验'"
          ></textarea>
        </label>
        <div class="form-actions">
          <button class="primary" :disabled="busy">保存核验</button>
        </div>
      </form></Modal
    >
  </div>
</template>
<script>
import Modal from "./Modal.vue";
import { api } from "../api";
import {
  state,
  kinds,
  extension,
  favorite,
  run,
  notify,
  newPlan,
  newCalendar,
  refreshPersonal,
  refreshCatalog,
} from "../store";
export default {
  components: { Modal },
  data: () => ({ state, kinds, busy: false }),
  computed: {
    detailedPeople() {
      return state.records.filter((r) => r.recordType === "detail");
    },
    opportunities() {
      return state.records.filter((r) => r.kind === "opportunity");
    },
  },
  methods: {
    extension,
    favorite,
    run,
    fromRecord() {
      const r = state.detail;
      state.detail = null;
      newPlan({
        title: r.title + " · 成长计划",
        domain: r.domain.slice(0, 60),
        personId: r.kind === "person" && r.recordType === "detail" ? r.id : "",
        opportunityId: r.kind === "opportunity" ? r.id : "",
      });
    },
    calendarRecord() {
      const r = state.detail;
      state.detail = null;
      newCalendar({ title: "准备：" + r.title, recordId: r.id });
    },
    reviewRecord() {
      const r = state.detail;
      state.review = { ...extension(r.id), recordId: r.id };
      state.detail = null;
    },
    download() {
      const r = state.detail;
      const text = [
        r.title,
        "资料编号：" + r.id,
        "来源：" + r.source,
        "仅为平台资料摘要，不是原始附件。",
        ...Object.entries(r.payload).map(([k, v]) => k + "：" + v),
      ].join("\n\n");
      const url = URL.createObjectURL(
        new Blob([text], { type: "text/plain;charset=utf-8" }),
      );
      const a = document.createElement("a");
      a.href = url;
      a.download = r.id + "-资料摘要.txt";
      a.click();
      setTimeout(() => URL.revokeObjectURL(url), 1000);
    },
    async saving(fn) {
      this.busy = true;
      await run(fn);
      this.busy = false;
    },
    async savePlan() {
      await this.saving(async () => {
        const p = {
          ...state.planDraft,
          deadline: state.planDraft.deadline || null,
        };
        await api(
          "/plans" + (p.id ? "/" + p.id : ""),
          p.id ? "PATCH" : "POST",
          p,
        );
        state.planDraft = null;
        await refreshPersonal();
        notify("发展计划已保存");
      });
    },
    async saveCalendar() {
      await this.saving(async () => {
        const e = state.calendarDraft;
        await api(
          "/calendar" + (e.id ? "/" + e.id : ""),
          e.id ? "PUT" : "POST",
          e,
        );
        state.calendarDraft = null;
        await refreshPersonal();
        notify("日程已保存");
      });
    },
    async deleteCalendar() {
      if (!confirm("确定删除这条个人日程？")) return;
      await this.saving(async () => {
        await api("/calendar/" + state.calendarDraft.id, "DELETE");
        state.calendarDraft = null;
        await refreshPersonal();
        notify("日程已删除");
      });
    },
    async saveReview() {
      await this.saving(async () => {
        await api(
          "/admin/records/" + state.review.recordId + "/review",
          "PUT",
          state.review,
        );
        state.review = null;
        await refreshCatalog();
        notify("核验信息已保存");
      });
    },
  },
};
</script>
