# CLAUDE.md

Guidance for working in this repository.

## Project

`muhfarming` — a mixed-farming management application. A Go REST API backend, an Android (Kotlin) frontend, and Terraform infrastructure on AWS.

```
backend/    # Go API server (module: muhfarming)
frontend/   # Android app (Kotlin, Gradle) — frontend/muhfarming/
terraform/  # AWS infrastructure (bootstrap/ then root)
Virdis/     # (untracked, not part of the build)
```

## Backend

Go 1.25, standard-library `net/http` router, GORM ORM over SQLite. No external web framework.

### Commands

```sh
make run-backend                    # from repo root: cd backend && go run ./cmd/api
cd backend && go build ./...        # compile
cd backend && go vet ./...          # vet
cd backend && go run ./cmd/api      # serve on :8080
```

Server listens on `:8080`. The DB path comes from the `DB_PATH` env var, defaulting to `./muhfarming.db` (gitignored) for local dev; in the container it is `/data/muhfarming.db`. There are currently no Go tests in the repo.

### Database persistence (prod)

SQLite is durable in prod via **Litestream** (single-writer; the app runs as a single instance). The container entrypoint is `litestream replicate -exec "/server"` (see `backend/Dockerfile` + `backend/litestream.yml`): on boot Litestream restores `/data/muhfarming.db` from S3 if missing, then runs the server and continuously replicates the WAL to `s3://muhfarming-data/db`. This survives both deploys (`docker rm` no longer wipes data) and instance loss. RPO is ~1 second.

Two things make this work: the DB lives on a **host bind mount** (`-v /opt/muhfarming/data:/data` in the deploy `docker run`), and the EC2 instance IAM role grants S3 access to the bucket (Terraform `aws_iam_role_policy.ec2_s3`) so no credentials are stored anywhere.

### Layout & conventions

Each domain entity lives in its own package under `backend/internal/<entity>/` and follows the same three-file slice:

- **`model.go`** — the GORM model (embeds `gorm.Model`, giving `ID`/`CreatedAt`/`UpdatedAt`/`DeletedAt`) plus `Create<Entity>Request` and `Update<Entity>Request` structs. JSON tags are camelCase; validation uses `validate:"..."` tags (`go-playground/validator`). Foreign keys are exposed as both an id (`FarmID` → `farmId`) and a nested association (`Farm farm.Farm` with `json:",omitempty"`).
- **`store.go`** — `Store` struct wrapping `*gorm.DB`. `NewStore(db)` calls `db.AutoMigrate(&Entity{})` and returns an error. Defines an unexported `store` interface (the handler depends on this, not the concrete type). Methods take `context.Context` and use `s.db.WithContext(ctx)`. Errors are wrapped with `fmt.Errorf("...: %w", err)`.
- **`handler.go`** — `Handler` holds a `store` interface; `NewHandler(s store)`. Standard CRUD methods (`List`, `Get`, `Create`, `Update`, `Delete`). Decodes JSON, runs `validate.Struct(req)`, calls `utils.WriteValidationError` on failure. `Create` returns `201`, `Delete` returns `204`.

Routes are wired manually in `backend/cmd/api/routes.go` using Go 1.22+ method+path patterns (e.g. `mux.HandleFunc("GET /farms/{id}", ...)`) and `r.PathValue("id")`. **Migration order matters:** base entities (no FKs) are registered before entities that reference them, since `NewStore` migrates on construction. When adding an entity, place its store creation after its dependencies.

`cmd/api/middleware.go` wraps the mux with `logRequests` (logs method, status, path, duration). `internal/db/db.go` opens the SQLite connection.

### Adding a new entity

1. Create `backend/internal/<entity>/{model,store,handler}.go` mirroring an existing slice (e.g. `hazard/` for a plain entity, `field/` for one with foreign keys).
2. Register the store + 5 routes in `routes.go`, respecting FK migration order.
3. Add the paths/schemas to `backend/internal/docs/openapi.yaml` (see below).

### API docs

The OpenAPI spec is hand-maintained at `backend/internal/docs/openapi.yaml` and embedded via `//go:embed`. Swagger UI is served at `GET /` and the raw spec at `GET /openapi.yaml`. **Keep the spec in sync manually** when routes or models change — it is not generated. `backend/redocly.yaml` lints it (some rules intentionally relaxed: no auth, no 4xx on list endpoints).

Soft deletes: `DELETE` sets `DeletedAt` and returns `204`; the row is excluded from later reads (a subsequent `GET` returns `404`).

Other endpoints: `GET /health`, `GET /forecast` (weather proxy).

### Notable domain notes

- `user.Role` is validated with `oneof=Admin Farmer`.
- The API currently has no authentication.

## Deployment

Push to `main` touching `backend/**` triggers `.github/workflows/backend.yml`: builds the Docker image, pushes to ECR (`eu-central-1`, repo `muhfarming`), and deploys to an EC2 instance (tagged `Name=muhfarming`) via SSM `docker run` on port 8080.

The Dockerfile builds with `CGO_ENABLED=1` (required by `go-sqlite3`) on `golang:1.25-alpine`, runtime on `alpine:3.20`.

Infrastructure is Terraform under `terraform/` (`bootstrap/` sets up remote state first, then the root config). `*.tfvars` and state are gitignored.

## Frontend

Android/Kotlin app in `frontend/muhfarming/` (Gradle, `gradlew`). Build with the Gradle wrapper from that directory.
