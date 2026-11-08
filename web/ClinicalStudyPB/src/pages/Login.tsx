import { useState, useContext, type FormEvent } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import { api } from '../services/api';

export function Login() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');
  const { login, logout, isAuthenticated } = useContext(AuthContext);

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    setErro('');
    try {
      const response = await api.post('/login', { email, senha });
      login(response.data.token || 'token-demonstracao');
      alert('Login realizado com sucesso!');
    } catch (err: any) {
      setErro('Falha na autenticação. Verifique suas credenciais.');
    }
  };

  if (isAuthenticated) {
    return (
      <div style={{ padding: '20px' }}>
        <h2>Você está autenticado!</h2>
        <button onClick={logout}>Sair (Logout)</button>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
      <h2>Login - ClinicalStudyPB</h2>
      {erro && <p style={{ color: 'red' }}>{erro}</p>}
      <form onSubmit={handleLogin}>
        <div style={{ marginBottom: '10px' }}>
          <label>E-mail:</label>
          <input
            type="email"
            style={{ width: '100%', padding: '8px' }}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div style={{ marginBottom: '10px' }}>
          <label>Senha:</label>
          <input
            type="password"
            style={{ width: '100%', padding: '8px' }}
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />
        </div>
        <button type="submit" style={{ padding: '10px 20px' }}>Entrar</button>
      </form>
    </div>
  );
}