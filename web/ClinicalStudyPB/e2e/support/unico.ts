// Sufixo único por execução (aaMMddHHmmss + 3 letras), para que o título dos estudos e o nome dos candidatos
// criados pelos testes nunca colidam, mesmo rodando a suíte várias vezes seguidas no mesmo banco (RQ-02 da T66).
export function sufixoUnico(): string {
  const agora = new Date().toISOString().replace(/\D/g, '').slice(2, 14)
  const aleatorio = Math.random().toString(36).replace(/[^a-z]/g, '').slice(0, 3).padEnd(3, 'x')
  return `${agora}${aleatorio}`
}

// Nome fictício único e legível para candidatos
export function nomeFicticio(prefixo: string): string {
  return `${prefixo} ${sufixoUnico()}`
}
