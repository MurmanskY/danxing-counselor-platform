<template>
  <div>
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">致每一位用心育人的你</p>
        <h1>辅导员荣誉<br />与职业发展导航</h1>
        <p>
          让育人积累被看见，让专业成长有路径。<br />在旦行，发现你的下一种可能。
        </p>
        <div class="hero-actions">
          <router-link class="primary" to="/routes"
            >探索成长路线 <span>↗</span></router-link
          ><router-link class="light-button" to="/manage/plans"
            >我的发展计划 →</router-link
          >
        </div>
        <div class="hero-note">
          <span class="tiny-dot"></span>以事实为基础，以成长为方向
        </div>
      </div>
      <div class="hero-visual">
        <span class="round-label">育人有方<br />成长有路</span
        ><img src="/danxing-campus.svg" alt="绿树环绕的校园建筑" />
        <div class="hero-caption">YOUR NEXT CHAPTER STARTS HERE</div>
      </div>
    </section>
    <section class="metrics">
      <div v-for="m in metrics" :key="m.label">
        <span class="metric-icon" :style="{ background: m.bg }">{{ m.icon }}</span
        ><span>{{ m.label }}</span
        ><strong><CountUp :value="m.value" /><small> 条</small></strong>
        <p>{{ m.note }}</p>
      </div>
    </section>
    <header class="page-heading">
      <p class="eyebrow">CHART YOUR OWN COURSE</p>
      <h1>每一次成长，都值得被看见</h1>
      <p>从你的阶段与兴趣出发，找到适合自己的下一步。</p>
    </header>
    <div class="notice">
      以下路线为平台发展建议，不作为岗位晋升或荣誉申报的资格标准。
    </div>
    <section
      v-for="(route, index) in roadmaps"
      :key="route.id"
      :class="['roadmap', route.color]"
    >
      <div class="roadmap-head">
        <span class="route-number">0{{ index + 1 }}</span>
        <div>
          <h2>{{ route.title }}</h2>
          <p>{{ route.subtitle }}</p>
        </div>
        <button
          @click="
            newPlan({
              title: route.title,
              domain: route.domain,
              tasks: route.steps.map((s) => s.title),
            })
          "
        >
          以此创建计划 ↗
        </button>
      </div>
      <div class="route-steps">
        <article v-for="(step, i) in route.steps" :key="step.title">
          <div class="step-line">
            <span>{{ i + 1 }}</span
            ><i></i>
          </div>
          <small>{{ step.label }}</small>
          <h3>{{ step.title }}</h3>
          <p>{{ step.detail }}</p>
          <button class="text-button" @click="browse(step.kind)">
            探索相关资料 →
          </button>
        </article>
      </div>
    </section>
  </div>
</template>
<script>
import { roadmaps } from "../roadmaps";
import { newPlan, state } from "../store";
import CountUp from "../components/CountUp.vue";
export default {
  components: { CountUp },
  data: () => ({ roadmaps, state }),
  computed: {
    metrics() {
      return [
        { label: "个人荣誉", kind: "honor", note: "记录育人路上的闪光", icon: "🏆", bg: "#fff4dc" },
        { label: "集体荣誉", kind: "collective", note: "汇聚共同前行的力量", icon: "🤝", bg: "#e9f2ff" },
        { label: "育人成果", kind: "outcome", note: "分享可借鉴的实践", icon: "🌟", bg: "#e5f8ee" },
        { label: "育人工作室", kind: "studio", note: "连接专业发展伙伴", icon: "🏠", bg: "#f0ebff" },
      ].map((m) => ({
        ...m,
        value: state.records.filter((r) => r.kind === m.kind).length,
      }));
    },
  },
  methods: {
    newPlan,
    browse(kind) {
      this.$router.push(
        {
          person: "/manage/people",
          opportunity: "/manage/opportunities",
          studio: "/manage/studios",
          outcome: "/resources",
          honor: "/honors",
        }[kind],
      );
    },
  },
};
</script>
