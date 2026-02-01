import { createRouter, createWebHistory } from "vue-router";
import OperadorasList from "../views/OperadorasList.vue";
import Dashboard from "../views/Dashboard.vue";
import OperadoraDetalhes from "../views/OperadoraDetalhes.vue";

const routes = [
  { path: "/", name: "Home", component: OperadorasList },
  { path: "/dashboard", name: "Dashboard", component: Dashboard },
  { path: "/operadora/:cnpj", name: "Detalhes", component: OperadoraDetalhes },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
