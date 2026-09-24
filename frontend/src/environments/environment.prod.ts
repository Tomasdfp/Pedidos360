export const environment = {
  production: true,
  azure: {
    clientId: 'd5cb7275-213c-45a3-8ae6-5c50731e29e1',
    tenantId: '7630b4fb-6df0-4113-9ed7-fb3287971a77',
    authority: 'https://pedidos360tg.ciamlogin.com/7630b4fb-6df0-4113-9ed7-fb3287971a77',
    redirectUri: 'https://tu-dominio-produccion.com',
    protectedResourceScopes: [
      'api://7b857d16-52ef-410a-ae0c-c93fb69ce170/pedidos.read',
      'api://7b857d16-52ef-410a-ae0c-c93fb69ce170/pedidos.write'
    ]
  },
  apiBaseUrl: 'https://tu-api-gateway.execute-api.us-east-1.amazonaws.com'
};