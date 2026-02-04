package com.desafio;

import com.desafio.repository.DatabaseRepository;
import com.desafio.service.EtlProcessor;
import com.desafio.service.ZipService;
import java.io.File;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- SISTEMA ETL AUTOMATIZADO ---");

        Path raiz = Paths.get("dados").toAbsolutePath();
        if (!Files.exists(raiz)) raiz = Paths.get("../dados").toAbsolutePath();

        Path pastaExtraidos = raiz.resolve("extraidos"); 
        Path arquivoConsolidado = raiz.resolve("consolidado_despesas.csv");
        Path arquivoOperadoras = raiz.resolve("Relatorio_cadop.csv"); // 
        Path relatorioAgregado = raiz.resolve("despesas_agregadas.csv"); 
        Path zipFinal = raiz.resolve("Teste_Beatriz_Kloss.zip");

        EtlProcessor processor = new EtlProcessor();
        DatabaseRepository db = new DatabaseRepository();
        ZipService zipService = new ZipService();

        try {
            System.out.println(">>> Iniciando limpeza do Banco de Dados...");
            db.limparTabela();
            
            if (!Files.exists(pastaExtraidos)) {
                Files.createDirectories(pastaExtraidos);
            }
            System.out.println(">>> 1. Procurando e extraindo arquivos ZIP...");
            File[] arquivosZip = raiz.toFile().listFiles((dir, nome) -> nome.toLowerCase().endsWith(".zip"));

            if (arquivosZip != null && arquivosZip.length > 0) {
                for (File zip : arquivosZip) {
                    System.out.println("   -> Extraindo: " + zip.getName());
                    zipService.descompactar(zip, pastaExtraidos);
                }
            } else {
                System.err.println("ERRO: Nenhum arquivo .zip encontrado na pasta 'dados'.");
                System.err.println("Por favor, coloque os arquivos da ANS lá.");
                return;
            }

            System.out.println(">>> 2. Processando CSVs extraídos e gravando no Banco...");
            processor.processarEGravar(pastaExtraidos, arquivoConsolidado, db);
            if (Files.exists(arquivoOperadoras)) {
                System.out.println(">>> 3. Carregando dados cadastrais (Relatorio_cadop.csv)...");
                processor.carregarOperadoras(arquivoOperadoras, db);
            } else {
                System.err.println("ALERTA: Arquivo de cadastro (Relatorio_cadop.csv) não encontrado.");
            }

            System.out.println(">>> 4. Gerando relatórios e ZIP final...");
            db.gerarRelatorioAgregado(relatorioAgregado);
            zipService.compactar(relatorioAgregado, zipFinal);

            System.out.println("SUCESSO! Arquivo de entrega gerado: " + zipFinal);

        } catch (Exception e) {
            System.err.println("Erro fatal no processamento: " + e.getMessage());
            e.printStackTrace();
        }
    }
}