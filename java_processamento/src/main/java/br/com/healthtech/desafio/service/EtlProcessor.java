package br.com.healthtech.desafio.service;

import br.com.healthtech.desafio.model.DadosFinanceiro;
import br.com.healthtech.desafio.model.Operadora;
import br.com.healthtech.desafio.repository.DatabaseRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class EtlProcessor {

    public void processarEGravar(Path pastaLeitura, Path arquivoSaida, DatabaseRepository db) {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(arquivoSaida, StandardCharsets.UTF_8))) {

            writer.println("CNPJ;RazaoSocial;Trimestre;Ano;ValorDespesas");

            File[] arquivos = pastaLeitura.toFile().listFiles((dir, nome) -> nome.toLowerCase().endsWith(".csv"));
            if (arquivos == null) return;

            for (File csv : arquivos) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv), StandardCharsets.UTF_8))) {
                    String linha;
                    while ((linha = br.readLine()) != null) {
                        if (ehLinhaDeDespesa(linha)) {
                            DadosFinanceiro dado = converterLinha(linha);
                            if (dado != null) {
                                escreverLinha(writer, dado);
                                db.salvar(dado);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processarUmArquivo(File csv, PrintWriter writer) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv), StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (ehLinhaDeDespesa(linha)) {
                    DadosFinanceiro dado = converterLinha(linha);
                    if (dado != null) {
                        escreverLinha(writer, dado);
                    }
                }
            }
        }
    }

    private boolean ehLinhaDeDespesa(String linha) {
        return linha.contains(";41;") || linha.contains("EVENTOS INDENIZÁVEIS LÍQUIDOS");
    }

    private DadosFinanceiro converterLinha(String linha) {
        String[] colunas = linha.split(";");
        if (colunas.length >= 6) {
            return new DadosFinanceiro(
                    colunas[1].replace("\"", ""), // RegAns
                    colunas[0].replace("\"", ""), // Data
                    colunas[3].replace("\"", ""), // Descricao
                    colunas[5].replace("\"", "").replace(",", ".") // Valor
            );
        }
        return null;
    }

    private void escreverLinha(PrintWriter writer, DadosFinanceiro dado) {
        String cnpj = dado.getRegAns();
        String nome = "Operadora " + dado.getRegAns();

        writer.printf("%s;%s;%s;%s;%s%n",
                cnpj, nome, dado.getTrimestre(), dado.getAno(), dado.getValor());
    }

    public void carregarOperadoras(Path arquivoCsv, DatabaseRepository db) {
        System.out.println("Lendo cadastro de operadoras...");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(arquivoCsv.toFile()), StandardCharsets.UTF_8))) {

            String linha;
            br.readLine();

            while ((linha = br.readLine()) != null) {
                String[] colunas = linha.split(";");
                if (colunas.length >= 11) {
                    String regAns = colunas[0].replace("\"", "").trim();
                    String cnpj = colunas[1].replace("\"", "").trim();
                    String razao = colunas[2].replace("\"", "").trim();
                    String uf = colunas[10].replace("\"", "").trim();

                    Operadora op = new Operadora(regAns, cnpj, razao, uf);
                    db.salvarOperadora(op);
                }
            }
            System.out.println("Cadastro de operadoras finalizado!");

        } catch (IOException e) {
            System.err.println("Erro ao ler operadoras: " + e.getMessage());
        }
    }
}