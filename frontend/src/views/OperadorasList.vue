<template>
  <div>
    <h1>Operadoras de Saúde</h1>

    <div class="card">
      <div class="search-box">
        <input
          v-model="busca"
          placeholder="Busque por Razão Social ou CNPJ..."
          @keyup.enter="buscarOperadoras"
        />
        <button @click="buscarOperadoras">
          <i class="fa-solid fa-magnifying-glass"></i> Buscar
        </button>
      </div>

      <div class="table-container">
        <table cellspacing="0">
          <thead>
            <tr>
              <th>CNPJ</th>
              <th>Razão Social</th>
              <th>UF</th>
              <th style="text-align: right">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="op in operadoras" :key="op.cnpj">
              <td>{{ op.cnpj }}</td>
              <td>
                <strong>{{ op.razao_social }}</strong>
              </td>
              <td>
                <span class="badge-uf">{{ op.uf }}</span>
              </td>

              <td style="text-align: right">
                <router-link
                  :to="{ name: 'Detalhes', params: { cnpj: op.cnpj } }"
                >
                  <button class="secondary">
                    <i class="fa-solid fa-eye"></i> Ver Detalhes
                  </button>
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination">
        <button
          class="secondary"
          :disabled="page <= 1"
          @click="mudarPagina(page - 1)"
        >
          <i class="fa-solid fa-chevron-left"></i> Anterior
        </button>

        <span>Página {{ page }}</span>

        <button
          class="secondary"
          :disabled="operadoras.length < limit"
          @click="mudarPagina(page + 1)"
        >
          Próxima <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>
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
