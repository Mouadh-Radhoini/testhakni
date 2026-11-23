// En dev (ng serve), les appels /api/** sont rediriges vers le backend Spring Boot.
// Port du backend : 8081 par defaut, ou la variable d'environnement API_PORT.
const port = process.env.API_PORT || 8081;

module.exports = {
  '/api': {
    target: `http://localhost:${port}`,
    secure: false,
    changeOrigin: true,
  },
  // Messagerie temps reel (STOMP sur WebSocket)
  '/ws': {
    target: `http://localhost:${port}`,
    secure: false,
    ws: true,
  },
};
