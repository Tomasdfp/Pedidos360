// server.js
const jsonServer = require('json-server');
const server = jsonServer.create();
const router = jsonServer.router('db.json');
const middlewares = jsonServer.defaults();

// Configurar CORS explícitamente ANTES de las rutas
server.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', 'http://localhost:4200');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, PATCH, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
  res.header('Access-Control-Allow-Credentials', 'true');

  // Manejar preflight OPTIONS
  if (req.method === 'OPTIONS') {
    return res.sendStatus(200);
  }

  next();
});

server.use(middlewares);

// Ruta principal: /api/pedidos → /pedidos
server.use('/api', router);

server.listen(8080, () => {
  console.log('=========================================');
  console.log('🚀 JSON Server corriendo en http://localhost:8080');
  console.log('📦 Endpoint: http://localhost:8080/api/pedidos');
  console.log('🔓 CORS habilitado para http://localhost:4200');
  console.log('=========================================');
});