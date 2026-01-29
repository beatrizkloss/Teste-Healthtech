package br.com.healthtech.desafio.model;

public class Operadora {
    private String regAns;
    private String cnpj;
    private String razaoSocial;
    private String uf;
    public Operadora(String regAns, String cnpj, String razaoSocial, String uf) {
        this.regAns = regAns;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.uf = uf;
    }

    public String getRegAns() {
        return regAns;
    }

    public String getCnpj() {
        return cnpj;
    }
    public String getRazaoSocial() {
        return razaoSocial;
    }
    public String getUf() {
        return uf;
    }

    public void setRegAns(String regAns) {
        this.regAns = regAns;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}