# notification-system

Skeleton de Notification System con arquitectura hexagonal y stubs para entender diseño sin integrar infraestructura real.

## Flujo

1. `POST /api/v1/notifications` recibe request.
2. `NotificationApplicationService` valida idempotencia.
3. Guarda notificacion en repositorio in-memory.
4. Encola ID en cola in-memory.
5. Worker (scheduler o endpoint manual) consume 1 mensaje.
6. Provider stub "envia" (solo log).
7. Estado pasa por `PENDING -> PROCESSING -> SENT` (o `FAILED`).

## Componentes stub

- Cache: `InMemoryIdempotencyCacheAdapter` (simula Redis)
- Queue: `InMemoryNotificationQueueAdapter` (simula Kafka/Rabbit)
- Repository: `InMemoryNotificationRepositoryAdapter` (simula DB)
- Provider: `StubNotificationDeliveryAdapter` (simula SES/Twilio/Firebase)

## Ejecutar

```bash
mvn spring-boot:run
```

## Probar

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H 'Content-Type: application/json' \
  -d '{
    "idempotencyKey": "booking-1001-email",
    "recipient": "alice@example.com",
    "subject": "Booking confirmed",
    "body": "Your booking #1001 is confirmed",
    "channel": "EMAIL"
  }'
```

El worker corre cada 1s. Tambien podes forzarlo:

```bash
curl -X POST http://localhost:8080/api/v1/workers/process-next
```

## Por que este skeleton

- Muestra separacion `domain/application/infrastructure`.
- Permite reemplazar stubs por adapters reales sin tocar el dominio.
- Expone patrones clave: idempotencia, async queue, worker, estado de entrega.
