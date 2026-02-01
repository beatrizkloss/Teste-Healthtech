<template>
  <div>
    <h1>Operadoras de Saúde</h1>

    <div class="search-box">
      <input
        v-model="busca"
        placeholder="Buscar por nome ou CNPJ..."
        @keyup.enter="buscarOperadoras"
      />
      <button @click="buscarOperadoras">🔍 Buscar</button>
    </div>

    <table border="1" width="100%" cellspacing="0" cellpadding="10">
      <thead>
        <tr>
          <th>CNPJ</th>
          <th>Razão Social</th>
          <th>UF</th>
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="op in operadoras" :key="op.cnpj">
          <td>{{ op.cnpj }}</td>
          <td>{{ op.razao_social }}</td>
          <td>{{ op.uf }}</td>
          <td>
            <router-link :to="{ name: 'Detalhes', params: { cnpj: op.cnpj } }">
              <button>Ver Detalhes</button>
            </router-link>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button :disabled="page <= 1" @click="mudarPagina(page - 1)">
        ⬅ Anterior
      </button>
      <span>Página {{ page }}</span>
      <button
        :disabled="operadoras.length < limit"
        @click="mudarPagina(page + 1)"
      >
        Próxima ➡
      </button>
    </div>
  </div>
</template>

<script>
import api from "../services/api";

export default {
  data() {
    return {
      operadoras: [],
      page: 1,
      limit: 10,
      busca: "",
    };
  },
  mounted() {
    this.buscarOperadoras();
  },
  methods: {
    async buscarOperadoras() {
      try {
        const response = await api.get("/operadoras", {
          params: {
            page: this.page,
            limit: this.limit,
            busca: this.busca,
          },
        });
        this.operadoras = response.data.data;
      } catch (error) {
        console.error("Erro ao buscar operadoras:", error);
        alert("Erro ao conectar com a API Python!");
      }
    },
    mudarPagina(novaPagina) {
      this.page = novaPagina;
      this.buscarOperadoras();
    },
  },
};
</script>

<style scoped>
.search-box {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}
input {
  padding: 8px;
  width: 300px;
  margin-right: 10px;
}
</style>
