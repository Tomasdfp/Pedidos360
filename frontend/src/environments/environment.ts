export const environment = {
  production: false,


  azure: {
    // Client ID de la app Frontend registrada en Entra External ID
    clientId: 'd5cb7275-213c-45a3-8ae6-5c50731e29e1',

    // Tenant ID del tenant Pedidos360TG
    tenantId: '7630b4fb-6df0-4113-9ed7-fb3287971a77',

    // Authority (URL base del tenant CIAM)
    authority: 'https://pedidos360tg.ciamlogin.com/7630b4fb-6df0-4113-9ed7-fb3287971a77',

    // URL a la que MSAL redirige después del login
    redirectUri: 'http://localhost:4200',

    // Scopes que el frontend solicitará al backend
    protectedResourceScopes: [
      'api://7b857d16-52ef-410a-ae0c-c93fb69ce170/pedidos.read',
      'api://7b857d16-52ef-410a-ae0c-c93fb69ce170/pedidos.write'
    ]
  },

  apiBaseUrl: 'http://localhost:8081'
};