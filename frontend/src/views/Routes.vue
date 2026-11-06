<template>
  <div>
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
import { newPlan } from "../store";
export default {
  data: () => ({ roadmaps }),
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
