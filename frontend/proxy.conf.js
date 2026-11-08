// En dev (ng serve), les appels /api/** sont rediriges vers le backend Spring Boot.
// Port du backend : 8080 par defaut, ou la variable d'environnement API_PORT
// (ex. si le port 8080 est deja pris par Oracle : $env:API_PORT = "8081").
const port = process.env.API_PORT || 8080;

module.exports = {
  '/api': {
    target: `http://localhost:${port}`,
    secure: false,
    changeOrigin: true,
  },
};
