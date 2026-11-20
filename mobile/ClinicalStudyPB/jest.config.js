// Preset do Expo para testes; o alias "@/" aponta para src/ (igual ao tsconfig.json)
module.exports = {
  preset: 'jest-expo',
  moduleNameMapper: { '^@/(.*)$': '<rootDir>/src/$1' },
  testPathIgnorePatterns: ['/node_modules/', '/.expo/'],
};
