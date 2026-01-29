package com.desafio;

import com.desafio.repository.DatabaseRepository;
import com.desafio.service.EtlProcessor;
import com.desafio.service.ZipService;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- SISTEMA ETL + CARGA DE DADOS ---");

        Path raiz = Paths.get("dados").toAbsolutePath();
        if (!Files.exists(raiz)) raiz = Paths.get("../dados").toAbsolutePath();

        Path extraidos = raiz.resolve("extraidos");
        Path consolidado = raiz.resolve("consolidado_despesas.csv");
        Path arquivoOperadoras = raiz.resolve("Relatorio_cadop.csv");

        Path relatorioAgregado = raiz.resolve("despesas_agregadas.csv");
        Path zipEntrega = raiz.resolve("Teste_Beatriz_Kloss.zip");

        EtlProcessor processor = new EtlProcessor();
        DatabaseRepository db = new DatabaseRepository();
        ZipService zipService = new ZipService();

        System.out.println("Preparando banco de dados...");
        db.limparTabela();

        System.out.println("1/3 - Carregando Despesas...");
        processor.processarEGravar(extraidos, consolidado, db);

        if (Files.exists(arquivoOperadoras)) {
            System.out.println("2/3 - Carregando Operadoras...");
            processor.carregarOperadoras(arquivoOperadoras, db);
        } else {
            System.err.println("ALERTA: Arquivo Relatorio_cadop.csv não encontrado!");
        }

        System.out.println("3/3 - Gerando Estatísticas e ZIP...");
        db.gerarRelatorioAgregado(relatorioAgregado);
        zipService.compactar(relatorioAgregado, zipEntrega);

        System.out.println("Processo Completo! Arquivo gerado: " + zipEntrega);
    }
}