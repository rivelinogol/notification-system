# notification-system

Skeleton de Notification System con arquitectura hexagonal y stubs in-memory para estudiar diseño sin levantar Kafka/Redis/Postgres.

## Que modela este proyecto

- Entrada HTTP con idempotencia.
- Encolado async de notificaciones.
- Worker que procesa cola.
- Enrutamiento por canal (`EMAIL`, `SMS`, `PUSH`).
- Templates por tipo (`BOOKING_CONFIRMED`, `BOOKING_CANCELLED`, `PAYMENT_FAILED`, `EVENT_REMINDER`).
- Reintentos con backoff exponencial.
- Reloj simulado in-memory para controlar el tiempo.
- Dead-letter store in-memory para mensajes agotados.

## Flujo

1. `POST /api/v1/notifications` recibe request.
2. Se valida idempotency key.
3. Se renderiza template (si no hay `customSubject/customBody`).
4. Se guarda `PENDING` en repo in-memory.
5. Se encola en queue in-memory con `availableAt = now`.
6. Worker procesa, marca `PROCESSING` y envia por provider stub.
7. Si falla:
   - `RETRY_PENDING` y re-queue con delay exponencial (`5s`, `10s`, `20s`, tope `60s`).
   - `DEAD_LETTER` si agota intentos.

## Endpoints

- `POST /api/v1/notifications`
- `GET /api/v1/notifications/{id}`
- `GET /api/v1/notifications/dead-letters`
- `POST /api/v1/workers/process-next`
- `POST /api/v1/workers/process-batch?maxItems=10`
- `GET /api/v1/simulation/clock`
- `POST /api/v1/simulation/clock/advance`
- `POST /api/v1/simulation/clock/reset`

## Ejemplo de request

```json
{
  "idempotencyKey": "booking-1001-email",
  "recipient": "alice@example.com",
  "channel": "EMAIL",
  "type": "BOOKING_CONFIRMED",
  "customSubject": null,
  "customBody": null
}
```

## Simular fallos de proveedor

Para ver retries y dead-letter:

- email: recipient que contenga `fail-email`
- sms: recipient que contenga `fail-sms`
- push: recipient que contenga `fail-push`

Ejemplo:

```json
{
  "idempotencyKey": "booking-999-fail",
  "recipient": "user-fail-email@example.com",
  "channel": "EMAIL",
  "type": "PAYMENT_FAILED"
}
```

## Simular avance de tiempo

Consultar reloj:

```bash
curl http://localhost:8080/api/v1/simulation/clock
```

Avanzar 30 segundos (destraba retries programados):

```bash
curl -X POST http://localhost:8080/api/v1/simulation/clock/advance \
  -H 'Content-Type: application/json' \
  -d '{"seconds": 30}'
```

Resetear reloj a tiempo real:

```bash
curl -X POST http://localhost:8080/api/v1/simulation/clock/reset
```

## Nota

No necesitas infraestructura externa para entender el diseño. Todo corre in-memory en este skeleton.
