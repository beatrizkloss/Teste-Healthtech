package com.desafio.model;

public class DadosFinanceiro {
    private String regAns;
    private String data;
    private String descricao;
    private String valor;

    public DadosFinanceiro(String regAns, String data, String descricao, String valor) {
        this.regAns = regAns;
        this.data = data;
        this.descricao = descricao;
        this.valor = valor;
    }


    public String getRegAns() {
        return regAns; }

    public String getData() {
        return data; }

    public String getValor() {
        return valor; }


    public String getAno() {
        return data.substring(0, 4);
    }

    public String getTrimestre() {
        int mes = Integer.parseInt(data.substring(5, 7));
        if (mes <= 3) return "1T";
        if (mes <= 6) return "2T";
        if (mes <= 9) return "3T";
        return "4T";
    }

    public String getDescricao() {
        return descricao;
    }

    public void setRegAns(String regAns) {
        this.regAns = regAns;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}