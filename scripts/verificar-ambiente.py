#!/usr/bin/env python3
"""T67: verifica o ambiente completo (docker compose) percorrendo o fluxo de planejamento pela API.

Roda contra o ambiente já no ar e não depende de nada além do Python 3 (só biblioteca padrão):

    docker compose up --build -d --wait
    python3 scripts/verificar-ambiente.py

Variáveis opcionais: WEB_URL (padrão http://localhost:5173) e API_URL (padrão http://localhost:8081).
Usa os usuários fictícios do seed V2 (senha "senha-de-teste") e cria um estudo novo a cada execução,
então pode ser repetido no mesmo banco. Sai com código 1 se alguma verificação falhar.
"""

import json
import os
import sys
import time
import urllib.error
import urllib.request

WEB = os.environ.get("WEB_URL", "http://localhost:5173").rstrip("/")
API = os.environ.get("API_URL", "http://localhost:8081").rstrip("/")
SENHA = "senha-de-teste"
SUFIXO = str(int(time.time()))

falhas = []
total = 0


def requisitar(metodo, caminho, corpo=None, token=None, base=API, cabecalhos=None):
    """Devolve (status, json ou texto, cabeçalhos) sem lançar exceção em 4xx/5xx."""
    dados = json.dumps(corpo).encode() if corpo is not None else None
    req = urllib.request.Request(base + caminho, data=dados, method=metodo)
    if corpo is not None:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    for nome, valor in (cabecalhos or {}).items():
        req.add_header(nome, valor)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            status, bruto, headers = resp.status, resp.read(), resp.headers
    except urllib.error.HTTPError as erro:
        status, bruto, headers = erro.code, erro.read(), erro.headers
    texto = bruto.decode("utf-8", "replace")
    try:
        return status, json.loads(texto) if texto else None, headers
    except json.JSONDecodeError:
        return status, texto, headers


def verificar(descricao, condicao, detalhe=""):
    global total
    total += 1
    if condicao:
        print(f"  ok    {descricao}")
    else:
        falhas.append(descricao)
        print(f"  FALHA {descricao}" + (f"  ->  {detalhe}" if detalhe else ""))


def esperado(descricao, resposta, status, codigo=None):
    """Confere o status HTTP e, se informado, o codigoRegra do corpo de erro."""
    recebido, corpo, _ = resposta
    codigo_recebido = corpo.get("codigoRegra") if isinstance(corpo, dict) else None
    ok = recebido == status and (codigo is None or codigo_recebido == codigo)
    verificar(descricao, ok, f"HTTP {recebido} {codigo_recebido or ''} {str(corpo)[:200]}")
    return corpo


def entrar(email):
    status, corpo, _ = requisitar("POST", "/api/auth/login", {"email": email, "senha": SENHA})
    verificar(f"login de {email}", status == 200 and corpo and corpo.get("token"), f"HTTP {status} {corpo}")
    return corpo.get("token") if isinstance(corpo, dict) else None


def secao(titulo):
    print(f"\n{titulo}")


secao(f"Ambiente (web {WEB}, API {API})")
status, corpo, _ = requisitar("GET", "/login", base=WEB)
verificar("web responde em /login", status == 200 and "<div id=\"root\"" in str(corpo), f"HTTP {status}")
status, corpo, _ = requisitar("GET", "/estudos/qualquer-rota-interna", base=WEB)
verificar("rota interna do React Router abre o index.html (sem 404 do nginx)", status == 200, f"HTTP {status}")
status, _, _ = requisitar("GET", "/v3/api-docs")
verificar("Swagger /v3/api-docs responde", status == 200, f"HTTP {status}")
status, _, headers = requisitar("OPTIONS", "/api/auth/login", cabecalhos={
    "Origin": WEB, "Access-Control-Request-Method": "POST", "Access-Control-Request-Headers": "Content-Type"})
verificar(f"CORS libera a origem da web ({WEB})", headers.get("Access-Control-Allow-Origin") == WEB,
          f"HTTP {status}, Allow-Origin={headers.get('Access-Control-Allow-Origin')}")

secao("Regressão RF01 a RF04 (login, usuários e estudos)")
esperado("login com senha errada é recusado (401)",
         requisitar("POST", "/api/auth/login", {"email": "admin@clinicalstudy.test", "senha": "errada-123"}), 401)
esperado("API sem token é recusada (401)", requisitar("GET", "/api/estudos"), 401)
admin = entrar("admin@clinicalstudy.test")
pesquisador = entrar("pesquisador@clinicalstudy.test")
coordenador = entrar("coordenador@clinicalstudy.test")
if not (admin and pesquisador and coordenador):
    print("\nSem os três logins não dá para seguir. Confira se o backend subiu com as migrations (docker compose logs backend).")
    sys.exit(1)
eu = esperado("GET /api/auth/me devolve o pesquisador", requisitar("GET", "/api/auth/me", token=pesquisador), 200)
id_pesquisador = eu.get("id") if isinstance(eu, dict) else None
verificar("perfil do /me é PESQUISADOR", isinstance(eu, dict) and eu.get("perfil") == "PESQUISADOR", str(eu))
esperado("administrador lista usuários", requisitar("GET", "/api/usuarios", token=admin), 200)
esperado("administrador cadastra usuário (201)", requisitar("POST", "/api/usuarios", {
    "nome": "Usuario T67 Ficticio", "email": f"t67.{SUFIXO}@clinicalstudy.test",
    "senha": "senha-ficticia-t67", "perfil": "COORDENADOR"}, token=admin), 201)
esperado("pesquisador não lista usuários (403)", requisitar("GET", "/api/usuarios", token=pesquisador), 403)
erro = esperado("estudo sem título é recusado com erro por campo (400)", requisitar("POST", "/api/estudos", {
    "objetivo": "Objetivo ficticio", "responsavelId": id_pesquisador, "limiteParticipantes": 5,
    "status": "PLANEJAMENTO"}, token=pesquisador), 400)
verificar("erro de validação traz errosCampo com o campo titulo",
          isinstance(erro, dict) and any(e.get("campo") == "titulo" for e in erro.get("errosCampo") or []), str(erro)[:200])
esperado("coordenador não cria estudo (403)", requisitar("POST", "/api/estudos", {
    "titulo": "Nao deve existir", "objetivo": "x", "responsavelId": id_pesquisador,
    "limiteParticipantes": 5, "status": "PLANEJAMENTO"}, token=coordenador), 403)

secao("Planejamento (RF05 a RF07)")
estudo = esperado("pesquisador cria estudo em PLANEJAMENTO (201)", requisitar("POST", "/api/estudos", {
    "titulo": f"Estudo T67 ficticio {SUFIXO}", "objetivo": "Verificar o ambiente Docker com dados ficticios.",
    "responsavelId": id_pesquisador, "dataInicio": "2026-12-01", "dataFim": "2027-06-01",
    "limiteParticipantes": 10, "status": "PLANEJAMENTO"}, token=pesquisador), 201)
estudo_id = estudo.get("id") if isinstance(estudo, dict) else None
if not estudo_id:
    print("\nSem o estudo não dá para seguir com o planejamento.")
    sys.exit(1)
base = f"/api/estudos/{estudo_id}"
verificar("estudo nasce em PLANEJAMENTO", estudo.get("status") == "PLANEJAMENTO", str(estudo.get("status")))
esperado("coordenador vê o estudo na listagem (200)", requisitar("GET", "/api/estudos", token=coordenador), 200)

esperado("avançar sem critério de inclusão é bloqueado (409)",
         requisitar("POST", f"{base}/transicoes", {"destino": "RECRUTAMENTO"}, token=pesquisador),
         409, "ESTUDO_SEM_CRITERIO_INCLUSAO")
esperado("pesquisador cadastra critério de inclusão (201)", requisitar("POST", f"{base}/criterios", {
    "tipo": "INCLUSAO", "descricao": "Ter dezoito anos ou mais (ficticio)", "peso": 1.0, "determinante": True},
    token=pesquisador), 201)
esperado("pesquisador cadastra critério de exclusão (201)", requisitar("POST", f"{base}/criterios", {
    "tipo": "EXCLUSAO", "descricao": "Impedimento simulado ao acompanhamento"}, token=pesquisador), 201)
esperado("critério repetido é recusado (409)", requisitar("POST", f"{base}/criterios", {
    "tipo": "INCLUSAO", "descricao": "  TER DEZOITO ANOS OU MAIS (FICTICIO) "}, token=pesquisador),
    409, "CRITERIO_DUPLICADO")
esperado("coordenador não cadastra critério (403)", requisitar("POST", f"{base}/criterios", {
    "tipo": "INCLUSAO", "descricao": "Nao deve existir"}, token=coordenador), 403)
status, criterios, _ = requisitar("GET", f"{base}/criterios", token=coordenador)
verificar("coordenador consulta os 2 critérios", status == 200 and isinstance(criterios, list) and len(criterios) == 2,
          f"HTTP {status} {str(criterios)[:200]}")

esperado("avançar sem visita é bloqueado (409)",
         requisitar("POST", f"{base}/transicoes", {"destino": "RECRUTAMENTO"}, token=pesquisador),
         409, "ESTUDO_SEM_VISITA_PREVISTA")
esperado("pesquisador cadastra visita (201)", requisitar("POST", f"{base}/visitas", {
    "nome": "Triagem", "diaPrevisto": 0, "tolerancia": 0}, token=pesquisador), 201)
esperado("visita no mesmo dia é recusada (409)", requisitar("POST", f"{base}/visitas", {
    "nome": "Outra triagem", "diaPrevisto": 0, "tolerancia": 0}, token=pesquisador), 409, "VISITA_DIA_DUPLICADO")
status, visitas, _ = requisitar("GET", f"{base}/visitas", token=coordenador)
verificar("coordenador consulta a visita", status == 200 and isinstance(visitas, list) and len(visitas) == 1,
          f"HTTP {status} {str(visitas)[:200]}")

esperado("coordenador não avança o estado (403)",
         requisitar("POST", f"{base}/transicoes", {"destino": "RECRUTAMENTO"}, token=coordenador), 403)
esperado("pular de PLANEJAMENTO para EXECUCAO é bloqueado (409)",
         requisitar("POST", f"{base}/transicoes", {"destino": "EXECUCAO"}, token=pesquisador),
         409, "TRANSICAO_ESTADO_INVALIDA")
avancado = esperado("pesquisador avança para RECRUTAMENTO (200)",
                    requisitar("POST", f"{base}/transicoes", {"destino": "RECRUTAMENTO"}, token=pesquisador), 200)
verificar("estudo agora está em RECRUTAMENTO", isinstance(avancado, dict) and avancado.get("status") == "RECRUTAMENTO",
          str(avancado)[:200])
esperado("critério depois do planejamento é bloqueado (409)", requisitar("POST", f"{base}/criterios", {
    "tipo": "EXCLUSAO", "descricao": "Condicao posterior ao planejamento"}, token=pesquisador), 409)
status, historico, _ = requisitar("GET", f"{base}/historico-status", token=coordenador)
verificar("histórico de estados registra o avanço",
          status == 200 and "RECRUTAMENTO" in json.dumps(historico), f"HTTP {status} {str(historico)[:200]}")

print(f"\n{total - len(falhas)} de {total} verificações passaram.")
if falhas:
    print("Falharam:")
    for descricao in falhas:
        print(f"  - {descricao}")
    sys.exit(1)
