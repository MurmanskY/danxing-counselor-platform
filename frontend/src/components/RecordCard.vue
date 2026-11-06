<template>
  <article class="record-card">
    <div class="card-top">
      <span class="kind-tag" :class="'kind-' + item.kind">{{ kinds[item.kind] }}</span
      ><button
        class="star"
        :class="{ active: state.favorites.includes(item.id) }"
        :aria-label="
          state.favorites.includes(item.id) ? '取消收藏' : '收藏资料'
        "
        :aria-pressed="state.favorites.includes(item.id)"
        @click="run(() => favorite(item.id))"
      >
        {{ state.favorites.includes(item.id) ? "★" : "☆" }}
      </button>
    </div>
    <button class="card-title" @click="showRecord(item)">
      <h3>{{ item.title }}</h3>
    </button>
    <p class="card-domain">{{ item.domain }}</p>
    <p class="card-summary">{{ summary }}</p>
    <div class="card-foot">
      <span class="review-status" :class="{ ok: reviewText === '已核验' }"
        ><i></i>{{ reviewText }}</span
      ><button class="text-button" @click="showRecord(item)">
        查看资料 <span>↗</span>
      </button>
    </div>
  </article>
</template>
<script>
import { state, kinds, run, favorite, extension, showRecord } from "../store";
export default {
  props: ["item"],
  data: () => ({ state, kinds }),
  computed: {
    reviewText() {
      return extension(this.item.id).reviewStatus;
    },
    summary() {
      const p = this.item.payload;
      const fields = Object.fromEntries(
        Object.entries(p).map(([key, value]) => [key.replace(/\s/g, ""), value]),
      );
      const first = (...keys) => keys.map((key) => fields[key]).find(Boolean);
      if (this.item.kind === "honor") {
        const recipient = first("获奖人", "获奖人员", "获奖者", "姓名");
        return recipient ? `获奖人：${recipient}` : "获奖人信息待补充";
      }
      if (this.item.kind === "collective") {
        const group = first("获奖集体/团队", "获奖集体", "获奖团队", "集体名称");
        return group ? `获奖集体：${group}` : "获奖集体信息待补充";
      }
      return (
        this.item.department ||
        first("获奖人", "获奖人员", "姓名", "负责人", "申报名称") ||
        this.item.source
      );
    },
  },
  methods: { run, favorite, extension, showRecord },
};
</script>
