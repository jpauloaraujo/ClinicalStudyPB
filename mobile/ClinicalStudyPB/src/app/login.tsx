import { useState } from 'react';
import { KeyboardAvoidingView, Platform, Pressable, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Botao, estilos } from '@/components/ui';
import { cores, espaco } from '@/components/tema';
import { useSessao } from '@/contexts/sessao';

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// M01 Login (docs/prototipos-mobile.md) — RF02
export default function LoginScreen() {
  const { entrar } = useSessao();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar() {
    if (!email.trim() || !senha) {
      setErro('Informe o e-mail e a senha.');
      return;
    }
    if (!EMAIL_VALIDO.test(email.trim())) {
      setErro('Informe um e-mail válido.');
      return;
    }
    setErro(null);
    setEnviando(true);
    try {
      await entrar(email.trim(), senha);
    } catch (falha) {
      setErro(falha instanceof Error ? falha.message : 'Erro inesperado.');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: cores.fundo }}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={{ flex: 1, justifyContent: 'center', padding: espaco[6], gap: espaco[4] }}
      >
        <View style={{ gap: espaco[1] }}>
          <Text style={estilos.titulo}>ClinicalStudyPB</Text>
          <Text style={estilos.textoSuave}>Aplicativo do participante</Text>
        </View>

        <View style={{ gap: espaco[1] }}>
          <Text style={estilos.texto}>E-mail</Text>
          <TextInput
            style={estilos.campo}
            value={email}
            onChangeText={setEmail}
            autoCapitalize="none"
            autoComplete="email"
            keyboardType="email-address"
            accessibilityLabel="E-mail"
          />
        </View>
        <View style={{ gap: espaco[1] }}>
          <Text style={estilos.texto}>Senha</Text>
          <TextInput
            style={estilos.campo}
            value={senha}
            onChangeText={setSenha}
            secureTextEntry={!mostrarSenha}
            autoComplete="password"
            accessibilityLabel="Senha"
            onSubmitEditing={enviar}
          />
          <Pressable onPress={() => setMostrarSenha((v) => !v)} accessibilityRole="button" style={{ paddingVertical: espaco[2] }}>
            <Text style={{ color: cores.primaria }}>{mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}</Text>
          </Pressable>
        </View>

        {erro && (
          <Text style={estilos.erro} accessibilityRole="alert">
            {erro}
          </Text>
        )}
        <Botao rotulo={enviando ? 'Entrando…' : 'Entrar'} onPress={enviar} desabilitado={enviando} />
        <Text style={[estilos.textoSuave, { fontSize: 13, textAlign: 'center' }]}>
          Sistema acadêmico — utiliza apenas dados fictícios.
        </Text>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
