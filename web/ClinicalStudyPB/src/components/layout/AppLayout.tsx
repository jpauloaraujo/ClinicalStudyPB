import { useState } from 'react'
import { Outlet } from 'react-router'
import { Header } from './Header'
import { Sidebar } from './Sidebar'
import './layout.css'

export function AppLayout() {
  const [menuAberto, setMenuAberto] = useState(false)

  return (
    <div className="app-layout">
      <Header menuAberto={menuAberto} onAlternarMenu={() => setMenuAberto((aberto) => !aberto)} />
      <div className="app-layout__corpo">
        <Sidebar aberto={menuAberto} onNavegar={() => setMenuAberto(false)} />
        <main className="app-layout__conteudo">
          <Outlet />
        </main>
      </div>
      <footer className="app-layout__rodape">
        Sistema acadêmico (UEPB — Engenharia de Software II) — utiliza apenas dados fictícios.
      </footer>
    </div>
  )
}
