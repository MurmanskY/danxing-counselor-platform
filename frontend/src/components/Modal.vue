<template>
  <div class="modal-scrim" @click.self="$emit('close')">
    <section
      ref="dialog"
      class="modal"
      role="dialog"
      aria-modal="true"
      :aria-label="title"
      tabindex="-1"
    >
      <header class="modal-head">
        <h2>{{ title }}</h2>
        <button
          class="icon-button"
          aria-label="关闭弹窗"
          @click="$emit('close')"
        >
          ×
        </button>
      </header>
      <slot />
    </section>
  </div>
</template>
<script>
export default {
  props: ["title"],
  mounted() {
    this.previous = document.activeElement;
    this.$refs.dialog.focus();
    document.addEventListener("keydown", this.key);
  },
  beforeDestroy() {
    document.removeEventListener("keydown", this.key);
    this.previous?.focus();
  },
  methods: {
    key(e) {
      if (e.key === "Escape") this.$emit("close");
      if (e.key === "Tab") {
        const els = this.$refs.dialog.querySelectorAll(
          "button:not(:disabled),input,textarea,select,a[href]",
        );
        const first = els[0],
          last = els[els.length - 1];
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    },
  },
};
</script>
