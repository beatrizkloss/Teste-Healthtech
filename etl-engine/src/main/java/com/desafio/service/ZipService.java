package com.desafio.service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipService {

    public void descompactar(File arquivoZip, Path destino) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(arquivoZip))) {
            ZipEntry entrada;
            while ((entrada = zis.getNextEntry()) != null) {
                Path caminhoArquivo = destino.resolve(entrada.getName());
                if (entrada.isDirectory()) {
                    Files.createDirectories(caminhoArquivo);
                } else {
                    Files.createDirectories(caminhoArquivo.getParent());
                    Files.copy(zis, caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public void compactar(Path origem, Path destino) {
        try (FileOutputStream fos = new FileOutputStream(destino.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(origem.toFile())) {

            ZipEntry zipEntry = new ZipEntry(origem.getFileName().toString());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            System.out.println("ZIP criado com sucesso: " + destino.getFileName());
        } catch (IOException e) {
            System.err.println("Erro ao zipar: " + e.getMessage());
        }
    }
}