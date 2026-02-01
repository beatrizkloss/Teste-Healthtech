from fastapi import FastAPI, HTTPException
from database import get_db_connection
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()
origins = [
    "http://localhost:5173",    
    "http://127.0.0.1:5173",   
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"], 
    allow_headers=["*"], 
)
@app.get("/")
def read_root():
    return {"status": "API Online", "docs": "/docs"}

# listar operadoras 
@app.get("/api/operadoras")
def listar_operadoras(page: int = 1, limit: int = 10, busca: str = None):
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Erro de conexão com o banco")
    
    cursor = conn.cursor(dictionary=True)
    offset = (page - 1) * limit
    
    sql = "SELECT cnpj, razao_social, uf FROM operadoras"
    params = []

    if busca:
        sql += " WHERE razao_social LIKE %s OR cnpj LIKE %s"
        termo = f"%{busca}%"
        params.extend([termo, termo])

    sql += " ORDER BY razao_social LIMIT %s OFFSET %s"
    params.extend([limit, offset])
    
    cursor.execute(sql, params)
    operadoras = cursor.fetchall()

    if busca:
        cursor.execute("SELECT COUNT(*) as total FROM operadoras WHERE razao_social LIKE %s OR cnpj LIKE %s", [termo, termo])
    else:
        cursor.execute("SELECT COUNT(*) as total FROM operadoras")
        
    total = cursor.fetchone()["total"]
    
    conn.close()
    
    return {
        "data": operadoras,
        "page": page,
        "limit": limit,
        "total": total
    }

# operadora pelo CNPJ 
@app.get("/api/operadoras/{cnpj}")
def detalhes_operadora(cnpj: str):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT * FROM operadoras WHERE cnpj = %s", [cnpj])
    operadora = cursor.fetchone()
    
    conn.close()
    
    if not operadora:
        raise HTTPException(status_code=404, detail="Operadora não encontrada")
        
    return operadora

# histórico de dispesas da operadora 
@app.get("/api/operadoras/{cnpj}/despesas")
def despesas_operadora(cnpj: str):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT reg_ans FROM operadoras WHERE cnpj = %s", [cnpj])
    operadora = cursor.fetchone()
    
    if not operadora:
        conn.close()
        raise HTTPException(status_code=404, detail="Operadora não encontrada")
    
    reg_ans = operadora["reg_ans"]

    query = """
        SELECT data_evento, descricao, vl_saldo_final 
        FROM demonstracoes_financeiras 
        WHERE reg_ans = %s 
        ORDER BY data_evento DESC
    """
    cursor.execute(query, [reg_ans])
    despesas = cursor.fetchall()
    
    conn.close()
    return despesas

# estatísticas gerais -- # POR ESTADO (UF) E TOP 5 OPERADORAS
@app.get("/api/estatisticas")
def estatisticas_gerais():
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT SUM(vl_saldo_final) as total_geral, AVG(vl_saldo_final) as media_geral FROM demonstracoes_financeiras")
    stats = cursor.fetchone()

    query_top5 = """
        SELECT o.razao_social, SUM(d.vl_saldo_final) as total
        FROM demonstracoes_financeiras d
        JOIN operadoras o ON d.reg_ans = o.reg_ans
        GROUP BY o.reg_ans
        ORDER BY total DESC
        LIMIT 5
    """
    cursor.execute(query_top5)
    top_5 = cursor.fetchall()

    query_uf = """
        SELECT o.uf, SUM(d.vl_saldo_final) as total
        FROM demonstracoes_financeiras d
        JOIN operadoras o ON d.reg_ans = o.reg_ans
        GROUP BY o.uf
        ORDER BY total DESC
    """
    cursor.execute(query_uf)
    por_uf = cursor.fetchall()
    
    conn.close()

    return {
        "total_despesas": stats["total_geral"],
        "media_despesas": stats["media_geral"],
        "top_5_operadoras": top_5, 
        "despesas_por_uf": por_uf  
    }