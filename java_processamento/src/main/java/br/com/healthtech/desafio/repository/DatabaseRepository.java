package br.com.healthtech.desafio.repository;

import br.com.healthtech.desafio.model.DadosFinanceiro;
import br.com.healthtech.desafio.model.Operadora;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseRepository {

    private static final String URL = "jdbc:mysql://localhost:3306/desafio_intu";
    private static final Dotenv dotenv = Dotenv.configure()
                            .ignoreIfMissing()
                            .load();

    private static final String USER = dotenv.get("DB_USER") != null ? dotenv.get("DB_USER") : "root";
    private static final String PASS = dotenv.get("DB_PASSWORD");

    public DatabaseRepository() {
        if (PASS == null) {
            throw new RuntimeException("ERRO FATAL: Senha do banco não configurada no arquivo .env!");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Erro: Driver não encontrado!");
        }
    }

    public void limparTabela() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            stmt.execute("TRUNCATE TABLE demonstracoes_financeiras");

            stmt.execute("DELETE FROM operadoras");

            System.out.println("Tabelas limpas! Pronto para nova carga.");

        } catch (SQLException e) {
            System.err.println("Erro ao limpar tabelas: " + e.getMessage());
        }
    }

    public void salvar(DadosFinanceiro dado) {
        String sql = "INSERT INTO demonstracoes_financeiras " +
                "(data_evento, reg_ans, cd_conta_contabil, descricao, vl_saldo_final) " +
                "VALUES (?, ?, '41', ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(dado.getData()));
            stmt.setString(2, dado.getRegAns());
            stmt.setString(3, dado.getDescricao());
            stmt.setDouble(4, Double.parseDouble(dado.getValor()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro de Banco: " + e.getMessage());
        }
    }

    public void salvarOperadora(Operadora op) {
        String sql = "INSERT INTO operadoras (reg_ans, cnpj, razao_social, uf) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, op.getRegAns());
            stmt.setString(2, op.getCnpj());
            stmt.setString(3, op.getRazaoSocial());
            stmt.setString(4, op.getUf());

            stmt.executeUpdate();

        } catch (SQLException e) {
            if (!e.getMessage().contains("Duplicate entry")) {
                System.err.println("Erro ao salvar operadora: " + e.getMessage());
            }
        }
    }

    public void gerarRelatorioAgregado(java.nio.file.Path arquivoSaida) {
        System.out.println("Gerando relatório agregado via SQL...");

        String sql = "SELECT o.razao_social, o.uf, " +
                "SUM(d.vl_saldo_final) as total, " +
                "AVG(d.vl_saldo_final) as media, " +
                "STDDEV(d.vl_saldo_final) as desvio " +
                "FROM demonstracoes_financeiras d " +
                "JOIN operadoras o ON d.reg_ans = o.reg_ans " +
                "WHERE d.data_evento >= '2024-01-01' " +
                "GROUP BY o.razao_social, o.uf " +
                "ORDER BY total DESC";

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(URL, USER, PASS);
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery();
             java.io.PrintWriter writer = new java.io.PrintWriter(java.nio.file.Files.newBufferedWriter(arquivoSaida, java.nio.charset.StandardCharsets.UTF_8))) {

            writer.println("RazaoSocial;UF;TotalDespesas;Media;DesvioPadrao");

            while (rs.next()) {
                writer.printf("%s;%s;%.2f;%.2f;%.2f%n",
                        rs.getString("razao_social"),
                        rs.getString("uf"),
                        rs.getDouble("total"),
                        rs.getDouble("media"),
                        rs.getDouble("desvio"));
            }
            System.out.println("Relatório gerado com segurança!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

