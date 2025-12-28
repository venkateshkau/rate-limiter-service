# rate-limiter-service

## 1. Description

A standalone rate limiting service built on Undertow.
Current state:
- HTTP server bootstrap
- Explicit routing
- Health endpoint for liveness verification

Rate-limiting logic and persistence are planned but not implemented yet.

## 2. How to run
To start a server (0.0.0.0) running on port 8080:
```shell
mvn clean compile
mvn exec:java
```
No docker yet, No k8s.

## 3. Health Check

This section must include:

- GET /health

#### Success example
```shell
curl -i http://localhost:8080/health
```

```shell
HTTP/1.1 200 OK

{"status":"ok"}
```


#### Failure example
```shell
curl -i http://localhost:8080/somethingelse
```

```shell
HTTP/1.1 404 Not Found

{"status":"Not Found"}
```
---

## 4. API Contract (DOCUMENT ONLY, NOT IMPLEMENTED YET)

```shell
POST /v1/check
```


#### Request example
```json
{
  "key": "user-123",
  "algorithm": "TOKEN_BUCKET",
  "limit": 100,
  "windowSeconds": 60
}
```
#### Response example
```json
{
  "allowed": true,
  "remaining": 42,
  "resetAtEpochMs": 1735689600000,
  "limit": 100
}

```

---
## 5. Non-Goals
- No admin UI
- No authentication beyond API key header
- No multi-region replication
- No persistance beyond Redis (when added).

---
## 6. ADRs (Architecture Decision Records)

Please refer to `docs/decisions/ADR-0001-tech-stack.md`

## Planned (Not implemented yet)

Here are the algorithms that will be supported by this service,
 - Token Bucket
 - Sliding Window
 - Fixed Window


This rate limiter service will support the following 
 - API Keys
 - Per-user limits
 - Bursty traffic
 - Distributed deployments

   
