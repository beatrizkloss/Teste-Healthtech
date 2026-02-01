<template>
  <div v-if="operadora">
    <button @click="$router.push('/')">⬅ Voltar</button>

    <div class="card">
      <h2>{{ operadora.razao_social }}</h2>
      <p><strong>CNPJ:</strong> {{ operadora.cnpj }}</p>
      <p><strong>UF:</strong> {{ operadora.uf }}</p>
      <p><strong>Registro ANS:</strong> {{ operadora.reg_ans }}</p>
    </div>

    <h3>Histórico de Despesas</h3>

    <div v-if="loadingDespesas">Carregando despesas...</div>

    <table
      v-else-if="despesas.length"
      border="1"
      width="100%"
      cellspacing="0"
      cellpadding="10"
    >
      <thead>
        <tr>
          <th>Data</th>
          <th>Descrição</th>
          <th>Valor</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(despesa, index) in despesas" :key="index">
          <td>{{ formatarData(despesa.data_evento) }}</td>
          <td>{{ despesa.descricao }}</td>
          <td class="valor">R$ {{ despesa.vl_saldo_final }}</td>
        </tr>
      </tbody>
    </table>

    <p v-else>Nenhuma despesa registrada para esta operadora.</p>
  </div>

  <div v-else>Carregando dados da operadora...</div>
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
.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}
.valor {
  font-family: monospace;
  color: #d32f2f;
}
button {
  margin-bottom: 20px;
  cursor: pointer;
  padding: 10px 20px;
}
</style>
