# Gestão de Operadoras ANS

## 🛠️ Tecnologias
| Módulo | Tecnologia |
| :--- | :--- |
| **ETL** | Java (Maven) |
| **Banco** | MySQL 8.0 |
| **Backend** | Python 3.10+ (FastAPI) |
| **Frontend** | Vue.js 3 (Vite) |

---

## 📸 Preview

![Preview-gif](https://github.com/user-attachments/assets/e7d8c6b4-7924-443b-a749-6e237cc82e9c)

---

## 🚀 Como Rodar o Projeto

### 1. Preparação dos Arquivos (Entrada)
Conforme as instruções de segurança, os dados da ANS não estão no repositório.

1. Na **raiz do projeto**, crie uma pasta chamada `dados`.
2. **Baixe os arquivos ZIP** (1T, 2T, 3T) das Demonstrações Contábeis da ANS [neste link](https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/2025/) e coloque dentro da pasta `dados`.
3. **Baixe o CSV de Operadoras** (`Relatorio_cadop.csv`) [neste link](https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/) e coloque também na pasta `dados`.

> **Nota:** Não é necessário extrair os ZIPs. O sistema Java fará isso automaticamente.

### 2. Banco de Dados
1. Crie um banco **MySQL** chamado `desafio_intu`.
2. Na raiz do projeto, crie um arquivo `.env` com suas credenciais:

```env
DB_HOST=localhost
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
DB_NAME=desafio_intu
DB_PORT=3306
```

4. Execute o script `database/init.sql` para criar a estrutura das tabelas.

### 3. Execução do ETL (Java)
1. Abra a pasta `etl-engine` na sua IDE.
2. Execute a classe `Main.java`.
   * O sistema irá detectar os ZIPs na pasta `dados`, extraí-los automaticamente, processar os CSVs, popular o banco e gerar o arquivo de entrega `Teste_Beatriz_Kloss.zip`.

### 4. Backend 
1. Entre na pasta `backend`: (cd backend)
   ```bash
   pip install -r requirements.txt
   uvicorn main:app --reload
   

### 5. Frontend 
1. Entre na pasta `frontend`: (cd frontend)
  ```bash
   npm install
   npm run dev 
```

---

---

## 🔌 Como Testar (Postman)


1.  Abra o **Postman**.
2.  Clique em **Import** e selecione o arquivo `postman_collection.json` (localizado na raiz do projeto).
3.  A coleção contém exemplos prontos para testar as rotas:
    * **Listar Operadoras** 
    * **Buscar por Nome** 
    * **Detalhes** 
    * **Dashboard** 

---

## ⚖️ Trade-offs e Decisões Arquiteturais


### 1. Processamento de Dados (ETL)

#### 1.2. Processamento de Arquivos
* **Decisão:** Processamento incremental (Streams).
* **Justificativa:** Os arquivos da ANS podem chegar a gigabytes, carregá-los inteiros na memória causaria erro de estouro de memória. Utilizei `BufferedReader` em Java para ler e processar linha a linha, mantendo o consumo de RAM baixo e estável.

#### 1.3. Tratamento de Inconsistências
* **Problema:** Os arquivos CSV de despesas possuem apenas o `REG_ANS` e não o CNPJ.
* **Decisão:** Mantive o `REG_ANS` como chave de ligação na etapa de leitura.
* **Justificativa:** Deleguei o enriquecimento (busca do CNPJ real) para a etapa de banco de dados, isso evita acoplamento excessivo no código de leitura e garante que a associação seja feita de forma íntegra no SGBD.

### 2. Transformação e Validação

#### 2.1. Tratamento de CNPJs Inválidos
* **Decisão:** Validação rigorosa e descarte.
* **Justificativa:** Registros com CNPJs inválidos (erro de formato ou dígito verificador) são descartados da importação e logados para auditoria. Isso garante a qualidade dos dados (*Data Quality*) no banco analítico, evitando "sujeira" nas consultas.

#### 2.2. Estratégia de Join (Enriquecimento)
* **Decisão:** Join via Banco de Dados (SQL).
* **Justificativa:** Realizar o cruzamento entre "Despesas" e "Dados Cadastrais" via SQL é mais performático do que fazer via código na memória, pois o MySQL utiliza índices otimizados para essa operação.

#### 2.3. Agregações (Soma e Média)
* **Decisão:** Cálculos no Banco de Dados (`SUM`, `AVG`, `STDDEV`).
* **Justificativa:** Transferi a carga de processamento matemático para o banco de dados, que é otimizado para agregações em grandes volumes, em vez de processar listas na memória da aplicação.

### 3. Banco de Dados

#### 3.2. Normalização
* **Decisão:** Tabelas Normalizadas (Opção B).
* **Justificativa:** Separei os dados em `operadoras` e `demonstracoes_financeiras`, isso evita repetir o texto da Razão Social e Endereço milhões de vezes na tabela de despesas, economizando armazenamento e facilitando atualizações cadastrais.

#### 3.2. Tipos de Dados
* **Monetário:** `DECIMAL(15,2)` (Para garantir precisão de centavos e evitar erros de arredondamento comuns em `FLOAT`).
* **Data:** `DATE` (Permite ordenação cronológica e extração de trimestres nativa do banco).

#### 3.4. Query Analítica
* **Abordagem:** Uso de CTEs (*Common Table Expressions*).
* **Justificativa:** Utilizei `WITH` para calcular a média global antes de cruzar com as operadoras, isso torna o SQL muito mais legível e fácil de manter do que subqueries aninhadas.

### 4. Backend (API)

#### 4.2.1. Framework
* **Decisão:** FastAPI (Opção B).
* **Justificativa:** Escolhido pela alta performance (assíncrono), validação de dados nativa e geração automática da documentação Swagger, facilitando os testes.

#### 4.2.2. Paginação
* **Decisão:** Offset-based (Opção A).
* **Justificativa:** Padrão de mercado para interfaces administrativas. Permite que o usuário pule diretamente para páginas específicas (ex: "Ir para página 5"), o que não seria trivial com paginação baseada em cursor.

#### 4.2.3. Cache
* **Decisão:** Cálculo sob demanda (Opção A).
* **Justificativa:** Como o banco está indexado, a query responde em milissegundos. Adicionar um Redis neste momento seria *overengineering* (complexidade desnecessária) para o escopo do teste.

### 5. Frontend (Interface)

#### 4.3.1. Estratégia de Busca
* **Decisão:** Busca no Servidor (Opção A).
* **Justificativa:** Filtrar no cliente exigiria baixar o banco de dados inteiro para o navegador, o que é inviável. A busca via SQL (`WHERE ... LIKE`) é escalável e mantém o frontend leve.

#### 4.3.2. Gerenciamento de Estado
* **Decisão:** Estado Local / Composition API (Opção A/C).
* **Justificativa:** A aplicação é simples e não justifica a complexidade de stores globais como Pinia ou Vuex. O estado é gerido localmente nas views, mantendo o código limpo.

#### 4.3.3. Performance da Tabela
* **Decisão:** Paginação no Backend.
* **Justificativa:** O navegador renderiza apenas 10 linhas por vez, isso garante que a interface permaneça rápida e responsiva, independentemente de existirem 100 ou 1 milhão de registros no banco.

## 👤 Autora

Feito por Beatriz Kloss.
