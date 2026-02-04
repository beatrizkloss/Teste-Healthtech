
--SCRIPT PARA CRIAÇÃO DAS TABELAS DO BANCO DE DADOS

--TABELA DE DEMONSTRAÇÕES FINANCEIRAS

CREATE TABLE demonstracoes_financeiras (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data_evento DATE,
    reg_ans VARCHAR(10),
    cd_conta_contabil VARCHAR(20),
    descricao VARCHAR(255),
    vl_saldo_final DECIMAL(15,2)
);

--TABELA DE OPERADORAS
CREATE TABLE operadoras (
    reg_ans VARCHAR(10) PRIMARY KEY,
    cnpj VARCHAR(20),
    razao_social VARCHAR(255)
);