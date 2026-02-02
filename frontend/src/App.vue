<template>
  <header class="navbar">
    <div class="navbar-content">
      <div class="logo">🏥 Gestão ANS</div>
      <nav>
        <router-link to="/">Operadoras</router-link>
        <router-link to="/dashboard">Dashboard</router-link>
      </nav>
    </div>
  </header>

  <main class="container">
    <router-view />
  </main>
</template>

<style>
.navbar {
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-content {
  max-width: 1100px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-weight: 700;
  font-size: 1.25rem;
  color: #2563eb;
  letter-spacing: -0.5px;
}

nav {
  display: flex;
  gap: 24px;
}

nav a {
  text-decoration: none;
  color: #6b7280;
  font-weight: 500;
  font-size: 0.95rem;
  padding: 22px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

nav a:hover {
  color: #111827;
}

nav a.router-link-active {
  color: #2563eb;
  border-bottom-color: #2563eb;
}
</style>
