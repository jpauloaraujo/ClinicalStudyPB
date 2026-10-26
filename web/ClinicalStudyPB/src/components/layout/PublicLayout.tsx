import { Outlet } from 'react-router'
import './layout.css'

export function PublicLayout() {
  return (
    <div className="public-layout">
      <main className="public-layout__cartao">
        <Outlet />
      </main>
      <p className="public-layout__aviso">Sistema acadêmico — utiliza apenas dados fictícios.</p>
    </div>
  )
}
