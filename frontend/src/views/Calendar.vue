<template>
  <div class="ambient-page calendar-page-bg">
    <header class="page-heading calendar-heading">
      <div>
        <p class="eyebrow cal-eyebrow">MAKE ROOM FOR GROWTH</p>
        <h1 class="cal-title"><span class="cal-title-icon">▦</span>发展日历</h1>
        <p class="cal-subtitle">聚焦未来可申报、可参与的发展机会。</p>
      </div>
      <div class="calendar-filters">
        <button
          v-for="f in filters"
          :key="f.value"
          :class="{ selected: filter === f.value }"
          @click="filter = f.value"
        >
          {{ f.label }}
        </button>
      </div>
    </header>
    <p class="calendar-note">
      <i class="info-icon">i</i
      >仅展示数据库中维护且尚未开始的事项，按时间顺序展示本月起未来 12 个月。
    </p>
    <div class="calendar-scroll">
      <div class="months" :key="filter">
        <section
          v-for="(month, mi) in calendarMonths"
          :key="month.year + '-' + month.month"
          class="month-column"
          :class="monthClasses(month, mi)"
          @click="selectedMonth = mi"
        >
          <header>
            <strong>{{ String(month.month).padStart(2, "0") }}</strong
            ><span>{{ month.year }} 年</span
            ><i v-if="month.isCurrent" class="month-now">本月</i>
            <em v-if="items(month).length" class="month-count"
              >{{ items(month).length }} 项</em
            >
          </header>
          <div v-if="!items(month).length" class="month-empty">
            <svg
              class="empty-cal"
              viewBox="0 0 24 24"
              width="18"
              height="18"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
              aria-hidden="true"
            >
              <rect x="3" y="5" width="18" height="16" rx="2.5" />
              <path d="M3 10h18M8 3v4M16 3v4" />
            </svg>
            暂无事项
          </div>
          <article
            v-for="(event, ei) in items(month)"
            :key="event.id"
            class="calendar-event"
            :class="kind(event)"
            :style="{
              animationDelay: mi * 0.03 + ei * 0.08 + 's',
              ...eventColor(event),
            }"
          >
            <small>{{ eventIcon(event) }} {{ dateLabel(event) }}</small>
            <h3>{{ event.title }}</h3>
            <span class="event-unit">⌖ {{ event.category }}</span>
            <span class="event-go">查看详情 →</span>
          </article>
        </section>
      </div>
    </div>
    <div class="calendar-legend">
      <span><i class="dot dot-city"></i>市级事项</span>
      <span><i class="dot dot-school"></i>校级事项</span>
    </div>
  </div>
</template>
<script>
import { state } from "../store";
export default {
  data: () => ({
    state,
    filter: "all",
    selectedMonth: 0,
    filters: [
      { label: "全部", value: "all" },
      { label: "市级事项", value: "city" },
      { label: "校级事项", value: "school" },
    ],
  }),
  computed: {
    calendarMonths() {
      const now = new Date();
      return Array.from({ length: 12 }, (_, offset) => {
        const date = new Date(now.getFullYear(), now.getMonth() + offset, 1);
        return {
          year: date.getFullYear(),
          month: date.getMonth() + 1,
          isCurrent: offset === 0,
        };
      });
    },
  },
  methods: {
    items(month) {
      const monthText = String(month.month).padStart(2, "0");
      return state.calendarEvents
        .filter(
          (event) =>
            event.eventDate.slice(0, 4) === String(month.year) &&
            event.eventDate.slice(5, 7) === monthText,
        )
        .filter(
          (event) => this.filter === "all" || this.kind(event) === this.filter,
        )
        .sort((a, b) => a.eventDate.localeCompare(b.eventDate));
    },
    monthClasses(month, mi) {
      return {
        current: month.isCurrent,
        filled: this.items(month).length > 0,
        selected: this.selectedMonth === mi,
      };
    },
    kind(event) {
      return event.category === "上海市" ? "city" : "school";
    },
    dateLabel(event) {
      const month = Number(event.eventDate.slice(5, 7));
      const day = Number(event.eventDate.slice(8, 10));
      return `${month}月${day}日`;
    },
    eventIcon(event) {
      if (/大赛|比赛|竞赛/.test(event.title)) return "🏆";
      if (/评选|最美|先进|标兵/.test(event.title)) return "🎖️";
      if (/培训|研修|沙龙/.test(event.title)) return "📚";
      return "📋";
    },
    eventColor(event) {
      const palette = [
        { bar: "#3f5fae", bg: "#eef2fd", fg: "#33549e" },
        { bar: "#2f8f83", bg: "#e9f5f3", fg: "#227065" },
        { bar: "#c08a2d", bg: "#faf3e4", fg: "#9c711d" },
        { bar: "#c05f74", bg: "#faedf0", fg: "#a04a5d" },
        { bar: "#7a62b8", bg: "#f1edf9", fg: "#5f4a9e" },
        { bar: "#4a9248", bg: "#ebf4eb", fg: "#3a7439" },
      ];
      let hash = 0;
      for (const ch of String(event.id)) hash = (hash * 31 + ch.charCodeAt(0)) % 997;
      const pick = palette[hash % palette.length];
      return {
        "--ev-bar": pick.bar,
        "--ev-bg": pick.bg,
        "--ev-fg": pick.fg,
      };
    },
  },
};
</script>
