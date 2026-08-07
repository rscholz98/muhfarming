package main

import (
	"muhfarming/internal/alert"
	"muhfarming/internal/auth"
	"muhfarming/internal/crop"
	"muhfarming/internal/cultivation"
	"muhfarming/internal/cultivationguideline"
	"muhfarming/internal/cultivationrisk"
	"muhfarming/internal/docs"
	"muhfarming/internal/farm"
	"muhfarming/internal/fertilizer"
	"muhfarming/internal/field"
	"muhfarming/internal/fieldcoordinate"
	"muhfarming/internal/hazard"
	"muhfarming/internal/incident"
	"muhfarming/internal/region"
	"muhfarming/internal/user"
	"muhfarming/internal/weather"
	"net/http"

	"gorm.io/gorm"
)

// setUpRoutes wires all handlers. Public routes (docs, health, auth) are served
// without authentication; every entity route requires a valid JWT, and writes
// to system/reference entities (plus user management) additionally require the
// admin role.
func setUpRoutes(db *gorm.DB, tokens *auth.TokenService, userStore *user.Store) (http.Handler, error) {
	// admin gates a handler behind the admin role (used inside RequireAuth).
	admin := auth.RequireAdmin

	// --- protected mux: everything here runs inside RequireAuth ---
	api := http.NewServeMux()

	weatherHandler := weather.NewHandler()
	api.HandleFunc("GET /forecast", weatherHandler.Forecast)

	// Farm — owned by the caller; farmers may fully manage their own farms.
	farmStore, err := farm.NewStore(db)
	if err != nil {
		return nil, err
	}
	farmHandler := farm.NewHandler(farmStore)
	api.HandleFunc("GET /farms", farmHandler.List)
	api.HandleFunc("GET /farms/{id}", farmHandler.Get)
	api.HandleFunc("POST /farms", farmHandler.Create)
	api.HandleFunc("PUT /farms/{id}", farmHandler.Update)
	api.HandleFunc("DELETE /farms/{id}", farmHandler.Delete)

	// Region — reference data. Read scoped to farmer's regions; writes admin-only.
	regionStore, err := region.NewStore(db)
	if err != nil {
		return nil, err
	}
	regionHandler := region.NewHandler(regionStore)
	api.HandleFunc("GET /regions", regionHandler.List)
	api.HandleFunc("GET /regions/{id}", regionHandler.Get)
	api.HandleFunc("POST /regions", admin(regionHandler.Create))
	api.HandleFunc("PUT /regions/{id}", admin(regionHandler.Update))
	api.HandleFunc("DELETE /regions/{id}", admin(regionHandler.Delete))

	// Cultivation — reference data. Read scoped; writes admin-only.
	cultivationStore, err := cultivation.NewStore(db)
	if err != nil {
		return nil, err
	}
	cultivationHandler := cultivation.NewHandler(cultivationStore)
	api.HandleFunc("GET /cultivations", cultivationHandler.List)
	api.HandleFunc("GET /cultivations/{id}", cultivationHandler.Get)
	api.HandleFunc("POST /cultivations", admin(cultivationHandler.Create))
	api.HandleFunc("PUT /cultivations/{id}", admin(cultivationHandler.Update))
	api.HandleFunc("DELETE /cultivations/{id}", admin(cultivationHandler.Delete))

	// Fertilizer — reference data readable by all; writes admin-only.
	fertilizerStore, err := fertilizer.NewStore(db)
	if err != nil {
		return nil, err
	}
	fertilizerHandler := fertilizer.NewHandler(fertilizerStore)
	api.HandleFunc("GET /fertilizers", fertilizerHandler.List)
	api.HandleFunc("GET /fertilizers/{id}", fertilizerHandler.Get)
	api.HandleFunc("POST /fertilizers", admin(fertilizerHandler.Create))
	api.HandleFunc("PUT /fertilizers/{id}", admin(fertilizerHandler.Update))
	api.HandleFunc("DELETE /fertilizers/{id}", admin(fertilizerHandler.Delete))

	// Hazard — reference data readable by all; writes admin-only.
	hazardStore, err := hazard.NewStore(db)
	if err != nil {
		return nil, err
	}
	hazardHandler := hazard.NewHandler(hazardStore)
	api.HandleFunc("GET /hazards", hazardHandler.List)
	api.HandleFunc("GET /hazards/{id}", hazardHandler.Get)
	api.HandleFunc("POST /hazards", admin(hazardHandler.Create))
	api.HandleFunc("PUT /hazards/{id}", admin(hazardHandler.Update))
	api.HandleFunc("DELETE /hazards/{id}", admin(hazardHandler.Delete))

	// Field — farmer-owned via their farm.
	fieldStore, err := field.NewStore(db)
	if err != nil {
		return nil, err
	}
	fieldHandler := field.NewHandler(fieldStore)
	api.HandleFunc("GET /fields", fieldHandler.List)
	api.HandleFunc("GET /fields/{id}", fieldHandler.Get)
	api.HandleFunc("POST /fields", fieldHandler.Create)
	api.HandleFunc("PUT /fields/{id}", fieldHandler.Update)
	api.HandleFunc("DELETE /fields/{id}", fieldHandler.Delete)

	// FieldCoordinate — farmer-owned via their field.
	fieldCoordinateStore, err := fieldcoordinate.NewStore(db)
	if err != nil {
		return nil, err
	}
	fieldCoordinateHandler := fieldcoordinate.NewHandler(fieldCoordinateStore)
	api.HandleFunc("GET /field-coordinates", fieldCoordinateHandler.List)
	api.HandleFunc("GET /field-coordinates/{id}", fieldCoordinateHandler.Get)
	api.HandleFunc("POST /field-coordinates", fieldCoordinateHandler.Create)
	api.HandleFunc("PUT /field-coordinates/{id}", fieldCoordinateHandler.Update)
	api.HandleFunc("DELETE /field-coordinates/{id}", fieldCoordinateHandler.Delete)

	// Crop — farmer-owned via their field.
	cropStore, err := crop.NewStore(db)
	if err != nil {
		return nil, err
	}
	cropHandler := crop.NewHandler(cropStore)
	api.HandleFunc("GET /crops", cropHandler.List)
	api.HandleFunc("GET /crops/{id}", cropHandler.Get)
	api.HandleFunc("POST /crops", cropHandler.Create)
	api.HandleFunc("PUT /crops/{id}", cropHandler.Update)
	api.HandleFunc("DELETE /crops/{id}", cropHandler.Delete)

	// CultivationRisk — system data. Read scoped to farmer's cultivations; writes admin-only.
	cultivationRiskStore, err := cultivationrisk.NewStore(db)
	if err != nil {
		return nil, err
	}
	cultivationRiskHandler := cultivationrisk.NewHandler(cultivationRiskStore)
	api.HandleFunc("GET /cultivation-risks", cultivationRiskHandler.List)
	api.HandleFunc("GET /cultivation-risks/{id}", cultivationRiskHandler.Get)
	api.HandleFunc("POST /cultivation-risks", admin(cultivationRiskHandler.Create))
	api.HandleFunc("PUT /cultivation-risks/{id}", admin(cultivationRiskHandler.Update))
	api.HandleFunc("DELETE /cultivation-risks/{id}", admin(cultivationRiskHandler.Delete))

	// CultivationGuideline — reference data readable by all; writes admin-only.
	cultivationGuidelineStore, err := cultivationguideline.NewStore(db)
	if err != nil {
		return nil, err
	}
	cultivationGuidelineHandler := cultivationguideline.NewHandler(cultivationGuidelineStore)
	api.HandleFunc("GET /cultivation-guidelines", cultivationGuidelineHandler.List)
	api.HandleFunc("GET /cultivation-guidelines/{id}", cultivationGuidelineHandler.Get)
	api.HandleFunc("POST /cultivation-guidelines", admin(cultivationGuidelineHandler.Create))
	api.HandleFunc("PUT /cultivation-guidelines/{id}", admin(cultivationGuidelineHandler.Update))
	api.HandleFunc("DELETE /cultivation-guidelines/{id}", admin(cultivationGuidelineHandler.Delete))

	// Incident — system data. Read scoped to farmer's regions; writes admin-only.
	incidentStore, err := incident.NewStore(db)
	if err != nil {
		return nil, err
	}
	incidentHandler := incident.NewHandler(incidentStore)
	api.HandleFunc("GET /incidents", incidentHandler.List)
	api.HandleFunc("GET /incidents/{id}", incidentHandler.Get)
	api.HandleFunc("POST /incidents", admin(incidentHandler.Create))
	api.HandleFunc("PUT /incidents/{id}", admin(incidentHandler.Update))
	api.HandleFunc("DELETE /incidents/{id}", admin(incidentHandler.Delete))

	// Alert — system data. Read scoped to farmer's fields; writes admin-only.
	alertStore, err := alert.NewStore(db)
	if err != nil {
		return nil, err
	}
	alertHandler := alert.NewHandler(alertStore)
	api.HandleFunc("GET /alerts", alertHandler.List)
	api.HandleFunc("GET /alerts/{id}", alertHandler.Get)
	api.HandleFunc("POST /alerts", admin(alertHandler.Create))
	api.HandleFunc("PUT /alerts/{id}", admin(alertHandler.Update))
	api.HandleFunc("DELETE /alerts/{id}", admin(alertHandler.Delete))

	// User management is admin-only.
	userHandler := user.NewHandler(userStore)
	api.HandleFunc("GET /users", admin(userHandler.List))
	api.HandleFunc("GET /users/{id}", admin(userHandler.Get))
	api.HandleFunc("POST /users", admin(userHandler.Create))
	api.HandleFunc("PUT /users/{id}", admin(userHandler.Update))
	api.HandleFunc("DELETE /users/{id}", admin(userHandler.Delete))

	// --- public routes (no auth) ---
	public := http.NewServeMux()

	docsHandler := docs.NewHandler()
	public.HandleFunc("GET /openapi.yaml", docsHandler.Spec)
	public.HandleFunc("GET /health", health)
	public.HandleFunc("GET /", docsHandler.UI) // root serves the UI; UI 404s non-root paths

	authHandler := auth.NewHandler(userStore, tokens)
	public.HandleFunc("POST /auth/signup", authHandler.Signup)
	public.HandleFunc("POST /auth/login", authHandler.Login)

	// The API mux (all entity routes) runs behind authentication.
	protected := tokens.RequireAuth(api)

	// Dispatch by path: known public paths bypass auth; everything else is a
	// protected API route. This avoids ServeMux pattern-conflict rules between
	// the catch-all "GET /" docs route and the method-less API prefixes.
	dispatch := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if isPublicPath(r.URL.Path) {
			public.ServeHTTP(w, r)
			return
		}
		protected.ServeHTTP(w, r)
	})

	return logRequests(dispatch), nil
}

// publicPrefixes are the request paths served without authentication.
var publicPrefixes = []string{
	"/openapi.yaml",
	"/health",
	"/auth/",
}

// isPublicPath reports whether a path should bypass authentication. The exact
// root "/" (docs UI) is public; all other unmatched paths fall through to the
// protected API (where RequireAuth returns 401, and the API mux 404s unknowns).
func isPublicPath(path string) bool {
	if path == "/" {
		return true
	}
	for _, p := range publicPrefixes {
		if path == p || (len(p) > 0 && p[len(p)-1] == '/' && len(path) >= len(p) && path[:len(p)] == p) {
			return true
		}
	}
	return false
}

func health(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.Write([]byte(`{"status":"ok"}`))
}
