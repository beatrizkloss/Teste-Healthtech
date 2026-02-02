<template>
  <div v-if="operadora">
    <button
      class="secondary"
      @click="$router.push('/')"
      style="margin-bottom: 20px"
    >
      <i class="fa-solid fa-arrow-left"></i> Voltar
    </button>

    <div class="card">
      <div class="header-info">
        <h2>{{ operadora.razao_social }}</h2>
        <span class="badge-active">Ativa</span>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <label>CNPJ</label>
          <p>{{ operadora.cnpj }}</p>
        </div>
        <div class="info-item">
          <label>Registro ANS</label>
          <p>{{ operadora.reg_ans }}</p>
        </div>
        <div class="info-item">
          <label>Localização</label>
          <p>{{ operadora.uf }}</p>
        </div>
      </div>
    </div>

    <h3>Histórico de Despesas</h3>

    <div v-if="loadingDespesas">Carregando despesas...</div>

    <div v-else class="card" style="padding: 0; overflow: hidden">
      <div class="table-container" style="border: none; box-shadow: none">
        <table cellspacing="0">
          <thead>
            <tr>
              <th>Data</th>
              <th>Descrição</th>
              <th style="text-align: right">Valor</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(despesa, index) in despesas" :key="index">
              <td>{{ formatarData(despesa.data_evento) }}</td>
              <td>{{ despesa.descricao }}</td>
              <td class="valor-negativo">R$ {{ despesa.vl_saldo_final }}</td>
            </tr>
            <tr v-if="despesas.length === 0">
              <td colspan="3" style="text-align: center; padding: 30px">
                Nenhuma despesa registrada.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <div v-else>Carregando dados...</div>
</template>

<script>
import api from "../services/api";

export default {
  data() {
    return {
      operadora: null,
      despesas: [],
      loadingDespesas: false,
    };
  },
  async mounted() {
    const cnpj = this.$route.params.cnpj;
    await this.carregarOperadora(cnpj);
    await this.carregarDespesas(cnpj);
  },
  methods: {
    async carregarOperadora(cnpj) {
      try {
        const response = await api.get(`/operadoras/${cnpj}`);
        this.operadora = response.data;
      } catch (error) {
        alert("Erro ao carregar operadora.");
      }
    },
    async carregarDespesas(cnpj) {
      this.loadingDespesas = true;
      try {
        const response = await api.get(`/operadoras/${cnpj}/despesas`);
        this.despesas = response.data;
      } catch (error) {
        console.error("Erro ao carregar despesas", error);
      } finally {
        this.loadingDespesas = false;
      }
    },
    formatarData(data) {
      if (!data) return "-";
      return new Date(data).toLocaleDateString("pt-BR");
    },
  },
};
</script>

<style scoped>
.header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.header-info h2 {
  margin: 0;
  color: #1e293b;
}

.badge-active {
  background-color: #dcfce7;
  color: #166534;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 700;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.info-item label {
  display: block;
  font-size: 0.8rem;
  color: #64748b;
  margin-bottom: 4px;
  text-transform: uppercase;
  font-weight: 600;
}

.info-item p {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 500;
  color: #0f172a;
}

.valor-negativo {
  text-align: right;
  color: #ef4444;
  font-family: monospace;
  font-weight: 600;
  font-size: 1rem;
}
</style>
