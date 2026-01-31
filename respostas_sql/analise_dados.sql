-- 3.2. DDL - CRIAÇÃO DAS TABELAS

CREATE TABLE IF NOT EXISTS operadoras (
                                          reg_ans INT PRIMARY KEY,
                                          cnpj VARCHAR(20) NOT NULL,
    razao_social VARCHAR(255),
    modalidade VARCHAR(100),
    logradouro VARCHAR(255),
    numero VARCHAR(50),
    complemento VARCHAR(150),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf CHAR(2),
    cep VARCHAR(20),
    telefone VARCHAR(50),
    email VARCHAR(150),
    representante VARCHAR(150),
    cargo_representante VARCHAR(100),
    data_registro DATE,
    INDEX idx_uf (uf)
    );

CREATE TABLE IF NOT EXISTS demonstracoes_financeiras (
                                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                         data_evento DATE NOT NULL,
                                                         reg_ans INT NOT NULL,
                                                         cd_conta_contabil VARCHAR(50) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    vl_saldo_final DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (reg_ans) REFERENCES operadoras(reg_ans),
    INDEX idx_data_evento (data_evento),
    INDEX idx_reg_ans (reg_ans)
    );

CREATE TABLE IF NOT EXISTS despesas_agregadas (
                                                  razao_social VARCHAR(255),
    uf CHAR(2),
    total_despesas DECIMAL(15,2),
    media_trimestral DECIMAL(15,2),
    desvio_padrao DECIMAL(15,2),
    PRIMARY KEY (razao_social, uf)
    );

-- 3.3. IMPORTAÇÃO DE DADOS

-- Carga Operadoras
-- LOAD DATA LOCAL INFILE '/var/lib/mysql-files/Relatorio_cadop.csv'
-- INTO TABLE operadoras
-- CHARACTER SET utf8mb4
-- FIELDS TERMINATED BY ';' ENCLOSED BY '"' LINES TERMINATED BY '\n'
-- IGNORE 1 ROWS
-- (reg_ans, cnpj, razao_social, modalidade, logradouro, numero, complemento, bairro, cidade, uf, cep, telefone, email, representante, cargo_representante, @var_data)
-- SET data_registro = STR_TO_DATE(@var_data, '%d/%m/%Y');

-- Carga Demonstrações Financeiras
-- LOAD DATA LOCAL INFILE '/var/lib/mysql-files/consolidado_despesas.csv'
-- INTO TABLE demonstracoes_financeiras
-- CHARACTER SET utf8mb4
-- FIELDS TERMINATED BY ';' LINES TERMINATED BY '\n'
-- IGNORE 1 ROWS
-- (data_evento, reg_ans, cd_conta_contabil, descricao, @var_valor)
-- SET vl_saldo_final = CAST(REPLACE(@var_valor, ',', '.') AS DECIMAL(15,2));


-- 3.4. QUERIES ANALÍTICAS


-- Query 1: top 5 operadoras com maior crescimento percentual (Último tri vs Primeiro tri)
SELECT
    o.razao_social,
    v_inicial.total AS despesa_inicial,
    v_final.total AS despesa_final,
    ROUND(((v_final.total - v_inicial.total) / v_inicial.total) * 100, 2) AS crescimento_percentual
FROM operadoras o
         JOIN (
    -- Busca total do primeiro trimestre disponível
    SELECT reg_ans, SUM(vl_saldo_final) as total
    FROM demonstracoes_financeiras
    WHERE data_evento = (SELECT MIN(data_evento) FROM demonstracoes_financeiras)
    GROUP BY reg_ans
) v_inicial ON o.reg_ans = v_inicial.reg_ans
         JOIN (
    -- Busca total do último trimestre disponível
    SELECT reg_ans, SUM(vl_saldo_final) as total
    FROM demonstracoes_financeiras
    WHERE data_evento = (SELECT MAX(data_evento) FROM demonstracoes_financeiras)
    GROUP BY reg_ans
) v_final ON o.reg_ans = v_final.reg_ans
WHERE v_inicial.total > 0
ORDER BY crescimento_percentual DESC
    LIMIT 5;

-- Query 2: top 5 UFs com maiores despesas totais + média por operadora
SELECT
    o.uf,
    SUM(d.vl_saldo_final) AS despesa_total_estado,
    AVG(d.vl_saldo_final) AS media_por_lancamento,
    COUNT(DISTINCT o.reg_ans) AS qtd_operadoras
FROM demonstracoes_financeiras d
         JOIN operadoras o ON d.reg_ans = o.reg_ans
GROUP BY o.uf
ORDER BY despesa_total_estado DESC
    LIMIT 5;

-- Query 3: operadoras com despesas acima da média geral em >= 2 trimestres
WITH MediaGlobal AS (
    SELECT AVG(total_trimestre) as valor_media
    FROM (
             SELECT reg_ans, data_evento, SUM(vl_saldo_final) as total_trimestre
             FROM demonstracoes_financeiras
             GROUP BY reg_ans, data_evento
         ) sub
),
     DespesasPorTrimestre AS (
         SELECT reg_ans, data_evento, SUM(vl_saldo_final) as total
         FROM demonstracoes_financeiras
         GROUP BY reg_ans, data_evento
     )
SELECT
    o.razao_social,
    COUNT(*) as trimestres_acima_da_media
FROM DespesasPorTrimestre dt
         JOIN operadoras o ON dt.reg_ans = o.reg_ans
         JOIN MediaGlobal mg ON 1=1
WHERE dt.total > mg.valor_media
GROUP BY o.razao_social
HAVING trimestres_acima_da_media >= 2;