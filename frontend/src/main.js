import Vue from "vue";
import VueRouter from "vue-router";
import App from "./App.vue";
import Home from "./views/Home.vue";
import Catalog from "./views/Catalog.vue";
import Routes from "./views/Routes.vue";
import Calendar from "./views/Calendar.vue";
import Plans from "./views/Plans.vue";
import Files from "./views/Files.vue";
import Manage from "./views/Manage.vue";
import Settings from "./views/Settings.vue";
import "./style.css";
Vue.use(VueRouter);
const router = new VueRouter({
  mode: "history",
  scrollBehavior: () => ({ x: 0, y: 0 }),
  routes: [
    { path: "/", component: Home },
    { path: "/honors", component: Catalog, props: { section: "honors" } },
    { path: "/routes", component: Routes },
    { path: "/calendar", component: Calendar },
    { path: "/resources", component: Catalog, props: { section: "resources" } },
    { path: "/manage/plans", component: Plans },
    { path: "/manage/files", component: Files },
    { path: "/settings", component: Settings },
    {
      path: "/manage/:section(people|opportunities|studios|favorites)",
      component: Catalog,
      props: (r) => ({ section: r.params.section }),
    },
    {
      path: "/manage/:section?",
      component: Manage,
      props: (r) => ({ section: r.params.section || "overview" }),
    },
    { path: "*", redirect: "/" },
  ],
});
new Vue({ router, render: (h) => h(App) }).$mount("#app");
