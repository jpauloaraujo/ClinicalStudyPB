import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

// Guarda o token com SecureStore no celular; na versão web (demonstração) usa localStorage
export const armazenamento = {
  async ler(chave: string): Promise<string | null> {
    if (Platform.OS === 'web') {
      try {
        return localStorage.getItem(chave);
      } catch {
        return null;
      }
    }
    return SecureStore.getItemAsync(chave);
  },

  async salvar(chave: string, valor: string): Promise<void> {
    if (Platform.OS === 'web') {
      try {
        localStorage.setItem(chave, valor);
      } catch {
        // Armazenamento indisponível: a sessão dura enquanto a página estiver aberta
      }
      return;
    }
    await SecureStore.setItemAsync(chave, valor);
  },

  async remover(chave: string): Promise<void> {
    if (Platform.OS === 'web') {
      try {
        localStorage.removeItem(chave);
      } catch {
        // Nada a remover
      }
      return;
    }
    await SecureStore.deleteItemAsync(chave);
  },
};
