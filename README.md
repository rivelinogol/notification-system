# notification-system

Skeleton de Notification System con arquitectura hexagonal y stubs in-memory para estudiar diseño sin levantar Kafka/Redis/Postgres.

## Que modela este proyecto

- Entrada HTTP con idempotencia.
- Encolado async de notificaciones.
- Worker que procesa cola.
- Enrutamiento por canal (`EMAIL`, `SMS`, `PUSH`).
- Templates por tipo (`BOOKING_CONFIRMED`, `BOOKING_CANCELLED`, `PAYMENT_FAILED`, `EVENT_REMINDER`).
- Reintentos con maximo de intentos.
- Dead-letter store in-memory para mensajes agotados.

## Flujo

1. `POST /api/v1/notifications` recibe request.
2. Se valida idempotency key.
3. Se renderiza template (si no hay `customSubject/customBody`).
4. Se guarda `PENDING` en repo in-memory.
5. Se encola en queue in-memory.
6. Worker procesa, marca `PROCESSING` y envia por provider stub.
7. Si falla:
   - `RETRY_PENDING` y re-queue si quedan intentos.
   - `DEAD_LETTER` si agota intentos.

## Endpoints

- `POST /api/v1/notifications`
- `GET /api/v1/notifications/{id}`
- `GET /api/v1/notifications/dead-letters`
- `POST /api/v1/workers/process-next`
- `POST /api/v1/workers/process-batch?maxItems=10`

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

## Nota

No necesitas infraestructura externa para entender el diseño. Todo corre in-memory en este skeleton.
