const app = db.getSiblingDB(process.env.MONGO_INITDB_DATABASE);
app.createUser({
  user: 'marketplace',
  pwd: process.env.MONGO_APP_PASSWORD,
  roles: [{role: 'readWrite', db: process.env.MONGO_INITDB_DATABASE}]
});
