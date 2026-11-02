"""Resumo da cobertura JaCoCo para o pipeline (T64).

Lê target/site/jacoco/jacoco.csv e imprime em Markdown o percentual de linhas do código de negócio
(domínio e serviços), que é a medida da regra de 80%, e o do backend inteiro, para informação.
Sai com código 1 se o código de negócio ficar abaixo do mínimo e a variável COBERTURA_BLOQUEAR for "true".
"""
import csv
import os
import sys

CSV = sys.argv[1] if len(sys.argv) > 1 else "target/site/jacoco/jacoco.csv"
MINIMO = float(os.environ.get("COBERTURA_MINIMA", "80"))
BLOQUEAR = os.environ.get("COBERTURA_BLOQUEAR", "false").lower() == "true"
NEGOCIO = ("UEPB.ESWII.ClinicalStudyPB.domain", "UEPB.ESWII.ClinicalStudyPB.service")


def percentual(linhas):
    coberta = sum(int(x["LINE_COVERED"]) for x in linhas)
    perdida = sum(int(x["LINE_MISSED"]) for x in linhas)
    total = coberta + perdida
    return (100 * coberta / total if total else 100.0), coberta, total


with open(CSV, encoding="utf-8") as arquivo:
    classes = list(csv.DictReader(arquivo))

negocio = [x for x in classes if x["PACKAGE"].startswith(NEGOCIO)]
pct_negocio, cob_n, tot_n = percentual(negocio)
pct_total, cob_t, tot_t = percentual(classes)

print("## Cobertura de testes do backend (JaCoCo)")
print()
print("| Escopo | Linhas cobertas | Percentual |")
print("|---|---|---|")
print(f"| **Código de negócio** (domínio e serviços) | {cob_n} de {tot_n} | **{pct_negocio:.1f}%** |")
print(f"| Backend inteiro (exceto classe main, config e DTOs) | {cob_t} de {tot_t} | {pct_total:.1f}% |")
print()
modo = "bloqueia abaixo do mínimo" if BLOQUEAR else "apenas informa (o bloqueio entra em 05/11)"
print(f"Mínimo de {MINIMO:.0f}% de linhas no código de negócio; modo atual: {modo}.")

if pct_negocio < MINIMO:
    print()
    print(f"> ⚠️ Cobertura do código de negócio ({pct_negocio:.1f}%) abaixo do mínimo de {MINIMO:.0f}%.")
    if BLOQUEAR:
        sys.exit(1)
