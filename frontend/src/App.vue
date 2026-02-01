<template>
  <div class="container">
    <nav class="navbar">
      <router-link to="/">📋 Lista de Operadoras</router-link> |
      <router-link to="/dashboard">📊 Estatísticas (Dashboard)</router-link>
    </nav>

    <router-view />
  </div>
</template>

<style>
.container {
  font-family: Arial, sans-serif;
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.navbar {
  padding: 20px;
  background-color: #f0f0f0;
  margin-bottom: 20px;
  border-radius: 8px;
}
.navbar a {
  text-decoration: none;
  color: #333;
  font-weight: bold;
  margin-right: 15px;
}
.navbar a.router-link-active {
  color: #42b983;
}
</style>
