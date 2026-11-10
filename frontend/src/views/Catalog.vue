<template>
  <div
    :class="{
      'ambient-page honors-page-bg': section === 'honors',
      'ambient-page resources-page-bg': section === 'resources',
    }"
  >
    <header class="page-heading">
      <p class="eyebrow">{{ info.english }}</p>
      <h1>{{ info.title }}</h1>
      <p>{{ info.subtitle }}</p>
    </header>
    <div class="catalog-toolbar">
      <div class="tabs">
        <button
          v-for="t in info.tabs"
          :key="t.value"
          :class="{ selected: tab === t.value }"
          @click="
            tab = t.value;
            page = 1;
          "
        >
          {{ t.label }}
        </button>
      </div>
      <span class="muted catalog-count">共 {{ filtered.length }} 条资料</span>
    </div>
    <div class="filters">
      <input
        v-model="query"
        type="search"
        placeholder="搜索名称、人员、领域或资料内容"
        aria-label="搜索资料"
        @input="page = 1"
      /><select v-model="domain" aria-label="按领域筛选" @change="page = 1">
        <option value="">全部领域</option>
        <option v-for="d in domains" :key="d">{{ d }}</option></select
      ><select
        v-if="section === 'honors'"
        v-model="level"
        aria-label="按级别筛选"
        @change="page = 1"
      >
        <option value="">全部级别</option>
        <option v-for="l in levels" :key="l">{{ l }}</option></select
      ><button
        v-if="query || domain || level"
        @click="
          query = '';
          domain = '';
          level = '';
          page = 1;
        "
      >
        重置
      </button>
    </div>
    <p v-if="section === 'opportunities'" class="notice">
      以下为历史项目目录，用于发现方向。当前申报资格、时间与材料要求仍需以当年正式通知为准。
    </p>
    <p v-if="section === 'people'" class="notice">
      4 份人员明细与 148
      条历史人员种子分别展示；历史人员尚待工号匹配，不按同名自动合并。
    </p>
    <div v-if="visible.length" class="card-grid" :key="tab + '-' + page">
      <RecordCard v-for="r in visible" :key="r.id" :item="r" />
    </div>
    <div v-else class="empty">
      <span>⌕</span>
      <h3>暂时没有匹配的资料</h3>
      <p>试试更短的关键词，或调整筛选条件。</p>
    </div>
    <div v-if="pages > 1" class="pagination">
      <button class="page-nav" :disabled="page === 1" @click="page--">
        ‹ 上一页</button
      ><button
        v-for="p in pageItems"
        :key="p.key"
        class="page-num"
        :class="{ current: p.n === page, ellipsis: p.n === '…' }"
        :disabled="p.n === '…'"
        @click="typeof p.n === 'number' && (page = p.n)"
      >
        {{ p.n }}</button
      ><button class="page-nav" :disabled="page === pages" @click="page++">
        下一页 ›
      </button>
    </div>
  </div>
</template>
<script>
import { state } from "../store";
import RecordCard from "../components/RecordCard.vue";
const configs = {
  honors: {
    title: "荣誉殿堂",
    english: "HONORS & ACHIEVEMENTS",
    subtitle: "每一份荣誉背后，都是日复一日的用心育人。",
    kinds: ["honor", "collective"],
    tabs: [
      ["", "全部荣誉"],
      ["honor", "个人荣誉"],
      ["collective", "集体荣誉"],
    ],
  },
  resources: {
    title: "资源中心",
    english: "RESOURCES FOR GROWTH",
    subtitle: "让优秀经验成为共同的资源，让每一次实践连接新的可能。",
    kinds: ["outcome", "studio"],
    tabs: [
      ["", "全部资源"],
      ["outcome", "育人成果"],
      ["studio", "育人工作室"],
    ],
  },
  people: {
    title: "人员画像",
    english: "PEOPLE & EXPERIENCE",
    subtitle: "呈现真实经历与育人方向，不做评分和排名。",
    kinds: ["person"],
    tabs: [
      ["detail", "人员明细"],
      ["legacy", "历史人员（待匹配）"],
    ],
  },
  opportunities: {
    title: "发展机会",
    english: "DISCOVER OPPORTUNITIES",
    subtitle: "从历史项目中发现发展方向，为未来机会做好准备。",
    kinds: ["opportunity"],
    tabs: [["", "历史项目目录"]],
  },
  studios: {
    title: "育人工作室",
    english: "STUDIOS & COLLABORATION",
    subtitle: "连接同行，在专业领域深耕，在协同实践中成长。",
    kinds: ["studio"],
    tabs: [["", "全部工作室"]],
  },
  favorites: {
    title: "我的收藏",
    english: "MY COLLECTION",
    subtitle: "把有启发的资料留下来，让成长有迹可循。",
    kinds: [],
    tabs: [["", "全部收藏"]],
  },
};
export default {
  components: { RecordCard },
  props: ["section"],
  data() {
    return {
      state,
      tab: this.section === "people" ? "detail" : "",
      query: "",
      domain: "",
      level: "",
      page: 1,
    };
  },
  computed: {
    info() {
      const c = configs[this.section] || configs.resources;
      return { ...c, tabs: c.tabs.map(([value, label]) => ({ value, label })) };
    },
    base() {
      return state.records
        .filter((r) =>
          this.section === "favorites"
            ? state.favorites.includes(r.id)
            : this.info.kinds.includes(r.kind),
        )
        .filter(
          (r) =>
            !this.tab ||
            (this.section === "people"
              ? r.recordType === this.tab
              : r.kind === this.tab),
        );
    },
    domains() {
      return [...new Set(this.base.map((r) => r.domain))].sort();
    },
    levels() {
      if (this.section === "honors") return ["市级以上", "市级", "校级"];
      return [
        ...new Set(
          this.base
            .map(
              (r) =>
                r.payload["奖励级别"] || r.payload["荣誉级别"] || r.payload["级别"],
            )
            .filter(Boolean),
        ),
      ];
    },
    filtered() {
      const q = this.query.trim().toLowerCase();
      return this.base.filter(
        (r) =>
          (!this.domain || r.domain === this.domain) &&
          (!this.level || this.honorLevelMatch(r)) &&
          (!q || JSON.stringify(r).toLowerCase().includes(q)),
      );
    },
    pages() {
      return Math.ceil(this.filtered.length / 12);
    },
    pageItems() {
      const total = this.pages;
      const current = this.page;
      const out = [];
      const push = (n) => out.push({ key: out.length + "-" + n, n });
      if (total <= 7) {
        for (let i = 1; i <= total; i++) push(i);
        return out;
      }
      push(1);
      if (current > 3) push("…");
      for (
        let i = Math.max(2, current - 1);
        i <= Math.min(total - 1, current + 1);
        i++
      )
        push(i);
      if (current < total - 2) push("…");
      push(total);
      return out;
    },
    visible() {
      return this.filtered.slice((this.page - 1) * 12, this.page * 12);
    },
  },
  methods: {
    honorLevelMatch(record) {
      const raw = String(
        record.payload["奖励级别"] ||
          record.payload["荣誉级别"] ||
          record.payload["级别"] ||
          "",
      );
      if (this.section !== "honors") return raw === this.level;
      if (this.level === "校级") return /校/.test(raw);
      if (this.level === "市级") return raw === "市级";
      // 市级以上：含市级以上、国家级、省级、部级
      return /市级以上|国|省|部/.test(raw);
    },
  },
};
</script>
