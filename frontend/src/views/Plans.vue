<template>
  <div>
    <header class="page-heading row-heading">
      <div>
        <p class="eyebrow">SMALL STEPS, LASTING GROWTH</p>
        <h1>我的发展计划</h1>
        <p>把方向写下来，让行动持续发生。</p>
      </div>
      <div class="button-row">
        <a href="/api/plans/export" class="button">导出 CSV</a
        ><button class="primary" @click="newPlan()">＋ 创建计划</button>
      </div>
    </header>
    <div class="tabs">
      <button
        v-for="t in ['全部', '进行中', '已完成']"
        :key="t"
        :class="{ selected: tab === t }"
        @click="tab = t"
      >
        {{ t }}
      </button>
    </div>
    <div v-if="!filtered.length" class="empty">
      <span>↗</span>
      <h3>从一个小目标开始</h3>
      <p>选择成长路线，或创建属于你自己的计划。</p>
      <button class="primary" @click="newPlan()">创建第一份计划</button>
    </div>
    <div class="plans-grid">
      <article v-for="v in filtered" :key="v.plan.id" class="plan-card">
        <div class="card-top">
          <span class="tag">{{ v.plan.domain }}</span
          ><span
            class="status"
            :class="{ complete: v.plan.status === '已完成' }"
            >{{ v.plan.status }}</span
          >
        </div>
        <h2>{{ v.plan.title }}</h2>
        <p class="muted">
          {{
            v.plan.deadline ? "目标日期 " + v.plan.deadline : "尚未设定目标日期"
          }}
        </p>
        <p>{{ v.plan.description }}</p>
        <div class="progress">
          <i :style="{ width: progress(v) + '%' }"></i>
        </div>
        <small
          >{{ v.tasks.filter((t) => t.done).length }} /
          {{ v.tasks.length }} 项任务已完成</small
        ><label v-for="task in v.tasks" :key="task.id" class="check-row"
          ><input
            type="checkbox"
            :checked="task.done"
            :disabled="pending === task.id"
            @change="toggle(task)"
          /><span :class="{ checked: task.done }">{{ task.title }}</span></label
        >
        <div class="card-foot">
          <button class="text-button" @click="edit(v.plan)">编辑计划</button
          ><button class="text-button danger" @click="remove(v.plan)">
            删除
          </button>
        </div>
      </article>
    </div>
  </div>
</template>
<script>
import { state, newPlan, run, refreshPersonal, notify } from "../store";
import { api } from "../api";
export default {
  data: () => ({ state, tab: "全部", pending: "" }),
  computed: {
    filtered() {
      return state.plans.filter(
        (v) => this.tab === "全部" || v.plan.status === this.tab,
      );
    },
  },
  methods: {
    newPlan,
    progress(v) {
      return Math.round(
        (v.tasks.filter((t) => t.done).length / v.tasks.length) * 100,
      );
    },
    edit(p) {
      state.planDraft = { ...p, deadline: p.deadline || "" };
    },
    async toggle(t) {
      this.pending = t.id;
      await run(async () => {
        await api("/tasks/" + t.id, "PATCH", { done: !t.done });
        await refreshPersonal();
      });
      this.pending = "";
    },
    async remove(p) {
      if (!confirm("确定删除“" + p.title + "”及其任务？")) return;
      await run(async () => {
        await api("/plans/" + p.id, "DELETE");
        await refreshPersonal();
        notify("计划已删除");
      });
    },
  },
};
</script>
