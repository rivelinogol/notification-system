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
- Preferencias de usuario in-memory (opt-out por canal/tipo).
- Quiet hours in-memory con defer de entrega.
- Dead-letter store in-memory para mensajes agotados.

## Flujo

1. `POST /api/v1/notifications` recibe request.
2. Se valida idempotency key.
3. Se renderiza template (si no hay `customSubject/customBody`).
4. Se revisan preferencias:
   - canal deshabilitado -> `SUPPRESSED`.
   - tipo deshabilitado -> `SUPPRESSED`.
5. Se aplica rate limiting por `recipient+channel`:
   - si excede: `SUPPRESSED` por rate limit.
6. Si pasa rate limiting:
   - se encola con `availableAt = now`.
   - o se encola diferido si aplica quiet hours.
7. Worker procesa, marca `PROCESSING` y envia por provider stub.
8. Si falla:
   - error retryable: `RETRY_PENDING` y re-queue con delay exponencial (`5s`, `10s`, `20s`, tope `60s`).
   - error non-retryable: `DEAD_LETTER` inmediato.
   - retryable sin intentos restantes: `DEAD_LETTER`.

## Endpoints

- `POST /api/v1/notifications`
- `GET /api/v1/notifications/{id}`
- `GET /api/v1/notifications/dead-letters`
- `GET /api/v1/notifications/stats`
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


## Metricas in-memory

`GET /api/v1/notifications/stats` devuelve:

- `submittedTotal`
- `duplicateTotal`
- `suppressedTotal`
- `rateLimitedTotal`
- `sentTotal`
- `retryScheduledTotal`
- `failedTotal`
- `deadLetterTotal`
- `sentLatencySamples`
- `averageSentLatencyMs`

## Simular fallos de proveedor

Para ver retries (errores retryable):

- email: recipient que contenga `fail-email`
- sms: recipient que contenga `fail-sms`
- push: recipient que contenga `fail-push`

Para ver errores no-retryable (se van directo a dead-letter):

- email: recipient que contenga `invalid-email`
- sms: recipient que contenga `invalid-sms`
- push: recipient que contenga `invalid-push`

## Simular preferencias (opt-out)

- `optout-all` -> suprime cualquier notificacion.
- `optout-email` / `optout-sms` / `optout-push` -> suprime por canal.
- `optout-booking-confirmed`
- `optout-booking-cancelled`
- `optout-payment-failed`
- `optout-event-reminder`

Ejemplo:

```json
{
  "idempotencyKey": "booking-2001-optout",
  "recipient": "user-optout-email@example.com",
  "channel": "EMAIL",
  "type": "BOOKING_CONFIRMED"
}
```


## Simular rate limiting

Rate limit in-memory: maximo `5` notificaciones por minuto por `recipient+channel`.

Si excede el limite:

- la notificacion queda `SUPPRESSED`
- se incrementa `rateLimitedTotal` en `/stats`

## Simular quiet hours

Si el recipient contiene `quiet-night`, aplica ventana silenciosa UTC `22:00-08:00`.
Durante esa ventana no se envia de inmediato: se agenda para las `08:00 UTC`.

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
