<template>
  <div>
    <h1>Dashboard Financeiro</h1>

    <div v-if="loading">Carregando estatísticas...</div>

    <div v-else class="dashboard-grid">
      <div class="left-col">
        <div class="kpi-card">
          <h3>Total de Despesas</h3>
          <p class="big-number">R$ {{ formatarValor(stats.total_despesas) }}</p>
          <p class="sub-text">
            Média: R$ {{ formatarValor(stats.media_despesas) }}
          </p>
        </div>

        <div class="top5-card">
          <h3>🏆 Top 5 Operadoras</h3>
          <ul>
            <li v-for="(op, index) in stats.top_5_operadoras" :key="index">
              <strong>{{ index + 1 }}.</strong>
              {{ op.razao_social.substring(0, 25) }}...
              <br />
              <span class="valor-pequeno"
                >R$ {{ formatarValor(op.total) }}</span
              >
            </li>
          </ul>
        </div>
      </div>

      <div class="chart-container">
        <h3>Distribuição por Estado (UF)</h3>
        <Bar v-if="chartData" :data="chartData" :options="chartOptions" />
      </div>
    </div>
  </div>
</template>

<script>
import api from "../services/api";
import { Bar } from "vue-chartjs";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
} from "chart.js";

ChartJS.register(
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
);

export default {
  components: { Bar },
  data() {
    return {
      loading: true,
      stats: null,
      chartData: null,
      chartOptions: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
      },
    };
  },
  async mounted() {
    try {
      const response = await api.get("/estatisticas");
      this.stats = response.data;
      this.prepararGrafico(response.data.despesas_por_uf);
    } catch (error) {
      console.error("Erro ao carregar dashboard", error);
    } finally {
      this.loading = false;
    }
  },
  methods: {
    formatarValor(valor) {
      if (!valor) return "0,00";
      return Number(valor).toLocaleString("pt-BR", {
        minimumFractionDigits: 2,
      });
    },
    prepararGrafico(dadosUF) {
      const ufs = dadosUF.map((item) => item.uf);
      const valores = dadosUF.map((item) => item.total);

      this.chartData = {
        labels: ufs,
        datasets: [
          {
            label: "Despesas (R$)",
            backgroundColor: "#3498db",
            data: valores,
          },
        ],
      };
    },
  },
};
</script>

<style scoped>
.dashboard-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: 1fr;
}
@media (min-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr 2fr;
  }
}

.left-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.kpi-card {
  background: #f8f9fa;
  padding: 20px;
  border-left: 5px solid #2ecc71;
  border-radius: 4px;
}
.big-number {
  font-size: 1.8em;
  font-weight: bold;
  color: #333;
  margin: 10px 0;
}
.sub-text {
  color: #666;
  font-size: 0.9em;
}

.top5-card {
  background: white;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
.top5-card ul {
  list-style: none;
  padding: 0;
}
.top5-card li {
  margin-bottom: 10px;
  border-bottom: 1px solid #eee;
  padding-bottom: 5px;
}
.valor-pequeno {
  color: #d32f2f;
  font-size: 0.85em;
  font-family: monospace;
}

.chart-container {
  background: white;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  height: 500px;
}
</style>
