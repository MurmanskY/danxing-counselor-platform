<template>
  <span>{{ current.toFixed(decimals) }}</span>
</template>
<script>
export default {
  props: { value: { type: [Number, String], default: 0 } },
  data: () => ({ current: 0, raf: null }),
  computed: {
    decimals() {
      return Number(this.value) % 1 ? 1 : 0;
    },
  },
  watch: {
    value: {
      immediate: true,
      handler(next) {
        this.animate(Number(next) || 0);
      },
    },
  },
  methods: {
    animate(target) {
      cancelAnimationFrame(this.raf);
      const from = this.current;
      const start = performance.now();
      const duration = 650;
      const step = (now) => {
        const k = Math.min(1, (now - start) / duration);
        const eased = 1 - Math.pow(1 - k, 3);
        this.current = from + (target - from) * eased;
        if (k < 1) this.raf = requestAnimationFrame(step);
        else this.current = target;
      };
      this.raf = requestAnimationFrame(step);
    },
  },
  beforeDestroy() {
    cancelAnimationFrame(this.raf);
  },
};
</script>
