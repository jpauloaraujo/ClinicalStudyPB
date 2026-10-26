# ClinicalStudyPB — Aplicação web

SPA em React 19 + TypeScript + Vite para os perfis **Administrador**, **Pesquisador** e **Coordenador**. O participante usa o app em `mobile/ClinicalStudyPB`.

```bash
npm install
npm run dev      # http://localhost:5173 (precisa do backend em http://localhost:8081)
npm run lint
npm run build    # verifica tipos e gera dist/
```

A URL da API vem de `VITE_API_URL` (copie `.env.example` para `.env.local`).

Organização das pastas, telas por perfil e credenciais de demonstração: [README principal](../../README.md), seções 2 e 5. Situação das tasks: [docs/status-sprint1.md](../../docs/status-sprint1.md).
