import { useRef, useState, type ComponentRef } from 'react';
import { KeyboardAvoidingView, Platform, Pressable, ScrollView, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { mensagemDoLogin } from '@/api/cliente';
import { Botao, estilos } from '@/components/ui';
import { cores, espaco } from '@/components/tema';
import { useSessao } from '@/contexts/sessao';

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// M01 Login (docs/prototipos-mobile.md) — RF02
export default function LoginScreen() {
  const { entrar, aviso } = useSessao();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const campoSenha = useRef<ComponentRef<typeof TextInput>>(null);
  // O estado só atualiza na próxima renderização; o ref bloqueia o segundo envio (toque duplo ou Enter)
  const emEnvio = useRef(false);

  async function enviar() {
    if (emEnvio.current) {
      return;
    }
    if (!email.trim() || !senha) {
      setErro('Informe o e-mail e a senha.');
      return;
    }
    if (!EMAIL_VALIDO.test(email.trim())) {
      setErro('Informe um e-mail válido.');
      return;
    }
    setErro(null);
    emEnvio.current = true;
    setEnviando(true);
    try {
      await entrar(email.trim(), senha);
    } catch (falha) {
      setErro(mensagemDoLogin(falha));
    } finally {
      emEnvio.current = false;
      setEnviando(false);
    }
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: cores.fundo }}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
        <ScrollView
          keyboardShouldPersistTaps="handled"
          contentContainerStyle={{ flexGrow: 1, justifyContent: 'center', padding: espaco[6], gap: espaco[4] }}
        >
          <View style={{ gap: espaco[1] }}>
            <Text style={estilos.titulo}>ClinicalStudyPB</Text>
            <Text style={estilos.textoSuave}>Aplicativo do participante</Text>
          </View>

          {aviso && (
            <Text style={estilos.texto} accessibilityRole="alert">
              {aviso}
            </Text>
          )}

          <View style={{ gap: espaco[1] }}>
            <Text style={estilos.texto}>E-mail</Text>
            <TextInput
              style={estilos.campo}
              value={email}
              onChangeText={(texto) => {
                setEmail(texto);
                setErro(null);
              }}
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="email"
              textContentType="emailAddress"
              keyboardType="email-address"
              returnKeyType="next"
              blurOnSubmit={false}
              onSubmitEditing={() => campoSenha.current?.focus()}
              accessibilityLabel="E-mail"
            />
          </View>
          <View style={{ gap: espaco[1] }}>
            <Text style={estilos.texto}>Senha</Text>
            <TextInput
              ref={campoSenha}
              style={estilos.campo}
              value={senha}
              onChangeText={(texto) => {
                setSenha(texto);
                setErro(null);
              }}
              secureTextEntry={!mostrarSenha}
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="password"
              textContentType="password"
              returnKeyType="go"
              onSubmitEditing={enviar}
              accessibilityLabel="Senha"
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
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
