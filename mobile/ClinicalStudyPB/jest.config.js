// Fixa o fuso dos testes: as datas do app seguem o fuso do aparelho e o resultado não pode depender da máquina (T117)
process.env.TZ = 'America/Sao_Paulo';

// Preset do Expo para testes; o alias "@/" aponta para src/ (igual ao tsconfig.json)
module.exports = {
  preset: 'jest-expo',
  moduleNameMapper: { '^@/(.*)$': '<rootDir>/src/$1' },
  testPathIgnorePatterns: ['/node_modules/', '/.expo/'],
};
