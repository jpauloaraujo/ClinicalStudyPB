#!/usr/bin/env python3
"""T68: verifica o recrutamento (RF08 a RF11) pela API, com os dados de demonstração (T63) e um estudo novo.

Roda contra o ambiente já no ar e só usa a biblioteca padrão do Python 3:

    docker compose up --build -d --wait      # perfil demo (padrão do compose)
    python3 scripts/verificar-recrutamento.py

Variável opcional: API_URL (padrão http://localhost:8081).

Duas partes:
  1. Dados de demonstração, só leitura: o estudo "Demo - Recrutamento com ultima vaga" (usuários demo.*), com
     listagem paginada, filtro por situação e contador de vagas. Nada é alterado, então a demonstração continua
     intacta (a última vaga é consumida só na apresentação).
  2. Estudo novo a cada execução, com os usuários do seed V2: cadastrar candidato, avaliar, consentir e incluir,
     nos casos de sucesso e de erro (estudo fora de recrutamento, candidato duplicado, avaliação incompleta,
     vagas esgotadas), as mensagens por código de erro e a regressão de login, usuários e estudos (Sprint 1).

Se o ambiente foi subido sem o perfil demo, a parte 1 é pulada com um aviso. Sai com código 1 se algo falhar.
"""

import json
import os
import sys
import time
import urllib.error
import urllib.request

API = os.environ.get("API_URL", "http://localhost:8081").rstrip("/")
SENHA = "senha-de-teste"
SUFIXO = str(int(time.time()))

falhas = []
total = 0


def requisitar(metodo, caminho, corpo=None, token=None):
    """Devolve (status, json ou texto) sem lançar exceção em 4xx/5xx."""
    dados = json.dumps(corpo).encode() if corpo is not None else None
    req = urllib.request.Request(API + caminho, data=dados, method=metodo)
    if corpo is not None:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            status, bruto = resp.status, resp.read()
    except urllib.error.HTTPError as erro:
        status, bruto = erro.code, erro.read()
    texto = bruto.decode("utf-8", "replace")
    try:
        return status, json.loads(texto) if texto else None
    except json.JSONDecodeError:
        return status, texto


def verificar(descricao, condicao, detalhe=""):
    global total
    total += 1
    if condicao:
        print(f"  ok    {descricao}")
    else:
        falhas.append(descricao)
        print(f"  FALHA {descricao}" + (f"  ->  {detalhe}" if detalhe else ""))


def esperado(descricao, resposta, status, codigo=None):
    """Confere o status HTTP e, se informado, o codigoRegra do corpo de erro; devolve o corpo."""
    recebido, corpo = resposta
    codigo_recebido = corpo.get("codigoRegra") if isinstance(corpo, dict) else None
    ok = recebido == status and (codigo is None or codigo_recebido == codigo)
    verificar(descricao, ok, f"HTTP {recebido} {codigo_recebido or ''} {str(corpo)[:200]}")
    return corpo


def entrar(email):
    status, corpo = requisitar("POST", "/api/auth/login", {"email": email, "senha": SENHA})
    if status == 200 and isinstance(corpo, dict) and corpo.get("token"):
        return corpo["token"]
    return None


def secao(titulo):
    print(f"\n{titulo}")


def criar_estudo_em_recrutamento(pesquisador, id_pesquisador, limite):
    estudo = esperado(f"estudo novo com {limite} vaga(s) (201)", requisitar("POST", "/api/estudos", {
        "titulo": f"Estudo T68 ficticio {SUFIXO}-{limite}", "objetivo": "Verificar o recrutamento com dados ficticios.",
        "responsavelId": id_pesquisador, "dataInicio": "2026-12-01", "dataFim": "2027-06-01",
        "limiteParticipantes": limite, "status": "PLANEJAMENTO"}, token=pesquisador), 201)
    estudo_id = estudo["id"]
    base = f"/api/estudos/{estudo_id}"
    esperado("critério de inclusão (201)", requisitar("POST", f"{base}/criterios", {
        "tipo": "INCLUSAO", "descricao": "Ter dezoito anos ou mais (ficticio)", "peso": 1.0, "determinante": True},
        token=pesquisador), 201)
    esperado("critério de exclusão (201)", requisitar("POST", f"{base}/criterios", {
        "tipo": "EXCLUSAO", "descricao": "Impedimento simulado ao acompanhamento"}, token=pesquisador), 201)
    esperado("visita de triagem (201)", requisitar("POST", f"{base}/visitas", {
        "nome": "Triagem", "diaPrevisto": 0, "tolerancia": 0}, token=pesquisador), 201)
    esperado("estudo avança para RECRUTAMENTO (200)",
             requisitar("POST", f"{base}/transicoes", {"destino": "RECRUTAMENTO"}, token=pesquisador), 200)
    return estudo_id


def respostas_da_avaliacao(estudo_id, coordenador, aprovar):
    _, criterios = requisitar("GET", f"/api/estudos/{estudo_id}/criterios", token=coordenador)
    return [{"criterioId": c["id"], "aplica": (c["tipo"] == "INCLUSAO") == aprovar} for c in criterios]


def candidato_pronto(estudo_id, coordenador, nome):
    """Cadastra, avalia como aprovado e registra o consentimento; devolve o id do candidato."""
    candidato = esperado(f"cadastra {nome} (201)", requisitar("POST", f"/api/estudos/{estudo_id}/candidatos", {
        "nomeFicticio": nome, "dataNascimento": "1990-04-12"}, token=coordenador), 201)
    cid = candidato["id"]
    esperado(f"avalia {nome} como aprovado (201)", requisitar("POST", f"/api/candidatos/{cid}/avaliacoes", {
        "respostas": respostas_da_avaliacao(estudo_id, coordenador, True)}, token=coordenador), 201)
    esperado(f"registra o consentimento de {nome} (201)",
             requisitar("POST", f"/api/candidatos/{cid}/consentimento", {}, token=coordenador), 201)
    return cid


# ---------------------------------------------------------------- 1. dados de demonstração (só leitura)
secao(f"1. Dados de demonstração (API {API})")
demo_coordenador = entrar("demo.coordenador@clinicalstudy.test")
if not demo_coordenador:
    print("  aviso: usuários demo.* não existem (ambiente sem o perfil demo); parte 1 pulada.")
else:
    ESTUDO_DEMO = "d6300000-0000-4000-8000-000000000102"
    estudo_demo = esperado("coordenador demo abre o estudo de recrutamento da demonstração (200)",
                           requisitar("GET", f"/api/estudos/{ESTUDO_DEMO}", token=demo_coordenador), 200)
    verificar("estudo demo está em RECRUTAMENTO com limite 2",
              isinstance(estudo_demo, dict) and estudo_demo.get("status") == "RECRUTAMENTO"
              and estudo_demo.get("limiteParticipantes") == 2, str(estudo_demo)[:200])
    pagina = esperado("lista os candidatos (200)", requisitar(
        "GET", f"/api/estudos/{ESTUDO_DEMO}/candidatos?pagina=0&tamanho=4", token=demo_coordenador), 200)
    verificar("a página traz 4 de 6 candidatos, 2 páginas",
              isinstance(pagina, dict) and len(pagina.get("conteudo", [])) == 4 and pagina.get("totalElementos") == 6
              and pagina.get("totalPaginas") == 2, str(pagina)[:200])
    segunda = esperado("segunda página (200)", requisitar(
        "GET", f"/api/estudos/{ESTUDO_DEMO}/candidatos?pagina=1&tamanho=4", token=demo_coordenador), 200)
    verificar("a segunda página traz os 2 restantes",
              isinstance(segunda, dict) and len(segunda.get("conteudo", [])) == 2, str(segunda)[:200])
    consentidos = esperado("filtro por situação CONSENTIDO (200)", requisitar(
        "GET", f"/api/estudos/{ESTUDO_DEMO}/candidatos?situacao=CONSENTIDO", token=demo_coordenador), 200)
    verificar("há 2 candidatos prontos para inclusão (CAND-004 e CAND-006)",
              isinstance(consentidos, dict) and sorted(c["codigo"] for c in consentidos.get("conteudo", []))
              == ["CAND-004", "CAND-006"], str(consentidos)[:200])
    vagas = esperado("contador de vagas (200)", requisitar(
        "GET", f"/api/estudos/{ESTUDO_DEMO}/participantes/vagas", token=demo_coordenador), 200)
    verificar("a demonstração começa com 1 de 2 vagas ocupadas",
              isinstance(vagas, dict) and vagas.get("limite") == 2 and vagas.get("incluidos") >= 1, str(vagas))
    demo_participante = entrar("demo.participante@clinicalstudy.test")
    esperado("participante não acessa a API de recrutamento (403)", requisitar(
        "GET", f"/api/estudos/{ESTUDO_DEMO}/candidatos", token=demo_participante), 403)

# ---------------------------------------------------------------- 2. estudo novo
secao("2. Login dos perfis (regressão da Sprint 1)")
esperado("senha errada é recusada (401)",
         requisitar("POST", "/api/auth/login", {"email": "admin@clinicalstudy.test", "senha": "errada-123"}), 401)
esperado("API sem token é recusada (401)", requisitar("GET", "/api/estudos"), 401)
pesquisador = entrar("pesquisador@clinicalstudy.test")
coordenador = entrar("coordenador@clinicalstudy.test")
admin = entrar("admin@clinicalstudy.test")
verificar("login dos três perfis do seed", bool(pesquisador and coordenador and admin))
if not (pesquisador and coordenador and admin):
    print("\nSem os três logins não dá para seguir. Confira se o backend subiu com as migrations.")
    sys.exit(1)
eu = esperado("GET /api/auth/me devolve o pesquisador", requisitar("GET", "/api/auth/me", token=pesquisador), 200)
id_pesquisador = eu["id"]
esperado("administrador lista usuários (200)", requisitar("GET", "/api/usuarios", token=admin), 200)
esperado("coordenador não lista usuários (403)", requisitar("GET", "/api/usuarios", token=coordenador), 403)

secao("3. Estudo fora de recrutamento")
planejamento = esperado("estudo em PLANEJAMENTO (201)", requisitar("POST", "/api/estudos", {
    "titulo": f"Estudo T68 em planejamento {SUFIXO}", "objetivo": "Sem recrutamento aberto.",
    "responsavelId": id_pesquisador, "limiteParticipantes": 3, "status": "PLANEJAMENTO"}, token=pesquisador), 201)
esperado("não cadastra candidato com o estudo em planejamento (409)", requisitar(
    "POST", f"/api/estudos/{planejamento['id']}/candidatos",
    {"nomeFicticio": "Fora De Recrutamento", "dataNascimento": "1990-01-01"}, token=coordenador),
    409, "ESTUDO_FORA_DE_RECRUTAMENTO")

secao("4. Sucesso: cadastrar, avaliar, consentir e incluir")
estudo = criar_estudo_em_recrutamento(pesquisador, id_pesquisador, 1)
base = f"/api/estudos/{estudo}"
primeiro = candidato_pronto(estudo, coordenador, f"Candidata Um {SUFIXO}")
participacao = esperado("coordenador inclui o candidato (201)", requisitar(
    "POST", f"{base}/participantes", {"candidatoId": primeiro}, token=coordenador), 201)
verificar("a inclusão traz código, data e quem incluiu",
          isinstance(participacao, dict) and participacao.get("codigo") and participacao.get("dataInclusao")
          and participacao.get("incluidoPor"), str(participacao))
ficha = esperado("a ficha do candidato passa a INCLUIDO (200)",
                 requisitar("GET", f"/api/candidatos/{primeiro}", token=coordenador), 200)
verificar("situação INCLUIDO", isinstance(ficha, dict) and ficha.get("situacao") == "INCLUIDO", str(ficha)[:200])

secao("5. Erros do recrutamento")
esperado("candidato duplicado (mesmo nome e data de nascimento) → 409", requisitar(
    "POST", f"{base}/candidatos", {"nomeFicticio": f"Candidata Um {SUFIXO}", "dataNascimento": "1990-04-12"},
    token=coordenador), 409, "CANDIDATO_DUPLICADO")
esperado("candidato já incluído não entra de novo → 409", requisitar(
    "POST", f"{base}/participantes", {"candidatoId": primeiro}, token=coordenador), 409, "CANDIDATO_JA_INCLUIDO")

segundo = esperado("cadastra o segundo candidato (201)", requisitar("POST", f"{base}/candidatos", {
    "nomeFicticio": f"Candidato Dois {SUFIXO}", "dataNascimento": "1985-07-30"}, token=coordenador), 201)["id"]
erro = esperado("avaliação incompleta (só um dos critérios) → 400", requisitar(
    "POST", f"/api/candidatos/{segundo}/avaliacoes",
    {"respostas": respostas_da_avaliacao(estudo, coordenador, True)[:1]}, token=coordenador), 400)
verificar("a mensagem diz que é preciso responder a todos os critérios",
          isinstance(erro, dict) and "todos os critérios" in (erro.get("mensagem") or ""), str(erro)[:200])
esperado("incluir sem avaliação → 409", requisitar(
    "POST", f"{base}/participantes", {"candidatoId": segundo}, token=coordenador), 409, "CANDIDATO_NAO_ELEGIVEL")
esperado("avalia o segundo como aprovado (201)", requisitar("POST", f"/api/candidatos/{segundo}/avaliacoes", {
    "respostas": respostas_da_avaliacao(estudo, coordenador, True)}, token=coordenador), 201)
esperado("incluir sem consentimento → 409", requisitar(
    "POST", f"{base}/participantes", {"candidatoId": segundo}, token=coordenador), 409, "CONSENTIMENTO_AUSENTE")
esperado("registra o consentimento do segundo (201)",
         requisitar("POST", f"/api/candidatos/{segundo}/consentimento", {}, token=coordenador), 201)
erro = esperado("vagas esgotadas (limite 1, já ocupada) → 409", requisitar(
    "POST", f"{base}/participantes", {"candidatoId": segundo}, token=coordenador), 409, "VAGAS_ESGOTADAS")
verificar("a mensagem cita o limite do estudo",
          isinstance(erro, dict) and "limite de 1" in (erro.get("mensagem") or ""), str(erro)[:200])
esperado("consentimento repetido → 409", requisitar(
    "POST", f"/api/candidatos/{segundo}/consentimento", {}, token=coordenador), 409, "CONSENTIMENTO_DUPLICADO")
esperado("pesquisador não avalia candidato (403)", requisitar(
    "POST", f"/api/candidatos/{segundo}/avaliacoes", {"respostas": []}, token=pesquisador), 403)
esperado("pesquisador não inclui participante (403)", requisitar(
    "POST", f"{base}/participantes", {"candidatoId": segundo}, token=pesquisador), 403)
esperado("pesquisador consulta os participantes (200)",
         requisitar("GET", f"{base}/participantes", token=pesquisador), 200)

secao("6. Paginação, filtros e estado do estudo")
for i in range(3):
    esperado(f"cadastra candidato extra {i + 1} (201)", requisitar("POST", f"{base}/candidatos", {
        "nomeFicticio": f"Extra {i + 1} {SUFIXO}", "dataNascimento": "1992-01-0" + str(i + 1)}, token=coordenador), 201)
pagina = esperado("lista com tamanho 2 (200)",
                  requisitar("GET", f"{base}/candidatos?pagina=0&tamanho=2", token=coordenador), 200)
verificar("5 candidatos no total em 3 páginas, 2 na primeira",
          isinstance(pagina, dict) and pagina.get("totalElementos") == 5 and pagina.get("totalPaginas") == 3
          and len(pagina.get("conteudo", [])) == 2, str(pagina)[:200])
cadastrados = esperado("filtro por situação CADASTRADO (200)",
                       requisitar("GET", f"{base}/candidatos?situacao=CADASTRADO", token=coordenador), 200)
verificar("3 candidatos ainda só cadastrados",
          isinstance(cadastrados, dict) and cadastrados.get("totalElementos") == 3, str(cadastrados)[:200])
vagas = esperado("contador de vagas (200)", requisitar("GET", f"{base}/participantes/vagas", token=coordenador), 200)
verificar("1 de 1 vagas, nenhuma restante",
          isinstance(vagas, dict) and vagas.get("incluidos") == 1 and vagas.get("restantes") == 0, str(vagas))
esperado("pesquisador avança o estudo para EXECUCAO (200)",
         requisitar("POST", f"{base}/transicoes", {"destino": "EXECUCAO"}, token=pesquisador), 200)
esperado("em execução não cadastra candidato (409)", requisitar(
    "POST", f"{base}/candidatos", {"nomeFicticio": f"Tardio {SUFIXO}", "dataNascimento": "1990-01-01"},
    token=coordenador), 409, "ESTUDO_FORA_DE_RECRUTAMENTO")
esperado("em execução não registra consentimento (409)", requisitar(
    "POST", f"/api/candidatos/{pagina['conteudo'][0]['id']}/consentimento", {}, token=coordenador),
    409)

print(f"\n{total - len(falhas)} de {total} verificações passaram.")
if falhas:
    print("Falharam:")
    for descricao in falhas:
        print(f"  - {descricao}")
    sys.exit(1)
