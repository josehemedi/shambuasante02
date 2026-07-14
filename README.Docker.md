# Conteneurisation Hospicloud (Shambua Santé)

## Démarrage

```bash
cd Hospicloud
cp .env.example .env
# Éditer les secrets (JWT, mots de passe MySQL/RabbitMQ)
docker compose up -d --build
```

Services :

| Service | URL |
|---------|-----|
| API Spring Boot | http://localhost:8082 |
| Health | http://localhost:8082/actuator/health |
| RabbitMQ Management | http://localhost:15672 |
| MySQL 8 (hôte) | localhost:3310 (évite le conflit avec MySQL local :3308) |

## Travaux asynchrones

Rapports et enregistrements passent par RabbitMQ.

```http
POST /api/async/reports
Authorization: Bearer <token>
Content-Type: application/json

{ "type": "REPORT_CAISSE_FACTURE", "entityId": 12, "payload": { "idFacture": 12 } }
```

Réponse **202 Accepted** :

```json
{ "jobId": "...", "status": "QUEUED", "statusUrl": "/api/async/jobs/...", "downloadUrl": "/api/async/jobs/.../download" }
```

Suivi :

```http
GET /api/async/jobs/{jobId}
GET /api/async/jobs/{jobId}/download
```

Enregistrement patient asynchrone :

```http
POST /api/patients/async
POST /api/patients?async=true
POST /api/async/enregistrements
```

Raccourcis métier (202) :

- `POST /api/tenant/cashier/factures/{id}/pdf/async`
- `POST /api/ordonnances/{id}/pdf/async`
- `GET .../pdf?async=true` (même comportement)

## Architecture Docker

- Build multi-stage Maven → JRE 17 Alpine
- Utilisateur non-root `hospicloud`
- Healthchecks Compose + Actuator
- Volumes persistants : MySQL, RabbitMQ, Redis, stockage PDF async
- Profile Spring `docker` (`application-docker.properties`)

## Arrêt / logs

```bash
docker compose logs -f backend
docker compose down
```
