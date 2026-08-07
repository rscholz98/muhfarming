package main

import (
	"muhfarming/internal/alert"
	"muhfarming/internal/crop"
	"muhfarming/internal/cultivation"
	"muhfarming/internal/cultivationguideline"
	"muhfarming/internal/cultivationrisk"
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

func setUpRoutes(db *gorm.DB) (http.Handler, error) {
	mux := http.NewServeMux()

	mux.HandleFunc("GET /health", health)

	weatherHandler := weather.NewHandler()
	mux.HandleFunc("GET /forecast", weatherHandler.Forecast)

	// Base entities (no foreign keys) are migrated first so that
	// dependent tables can reference them.
	farmStore, err := farm.NewStore(db)
	if err != nil {
		return nil, err
	}
	farmHandler := farm.NewHandler(farmStore)
	mux.HandleFunc("GET /farms", farmHandler.List)
	mux.HandleFunc("GET /farms/{id}", farmHandler.Get)
	mux.HandleFunc("POST /farms", farmHandler.Create)
	mux.HandleFunc("PUT /farms/{id}", farmHandler.Update)
	mux.HandleFunc("DELETE /farms/{id}", farmHandler.Delete)

	regionStore, err := region.NewStore(db)
	if err != nil {
		return nil, err
	}
	regionHandler := region.NewHandler(regionStore)
	mux.HandleFunc("GET /regions", regionHandler.List)
	mux.HandleFunc("GET /regions/{id}", regionHandler.Get)
	mux.HandleFunc("POST /regions", regionHandler.Create)
	mux.HandleFunc("PUT /regions/{id}", regionHandler.Update)
	mux.HandleFunc("DELETE /regions/{id}", regionHandler.Delete)

	cultivationStore, err := cultivation.NewStore(db)
	if err != nil {
		return nil, err
	}
	cultivationHandler := cultivation.NewHandler(cultivationStore)
	mux.HandleFunc("GET /cultivations", cultivationHandler.List)
	mux.HandleFunc("GET /cultivations/{id}", cultivationHandler.Get)
	mux.HandleFunc("POST /cultivations", cultivationHandler.Create)
	mux.HandleFunc("PUT /cultivations/{id}", cultivationHandler.Update)
	mux.HandleFunc("DELETE /cultivations/{id}", cultivationHandler.Delete)

	fertilizerStore, err := fertilizer.NewStore(db)
	if err != nil {
		return nil, err
	}
	fertilizerHandler := fertilizer.NewHandler(fertilizerStore)
	mux.HandleFunc("GET /fertilizers", fertilizerHandler.List)
	mux.HandleFunc("GET /fertilizers/{id}", fertilizerHandler.Get)
	mux.HandleFunc("POST /fertilizers", fertilizerHandler.Create)
	mux.HandleFunc("PUT /fertilizers/{id}", fertilizerHandler.Update)
	mux.HandleFunc("DELETE /fertilizers/{id}", fertilizerHandler.Delete)

	hazardStore, err := hazard.NewStore(db)
	if err != nil {
		return nil, err
	}
	hazardHandler := hazard.NewHandler(hazardStore)
	mux.HandleFunc("GET /hazards", hazardHandler.List)
	mux.HandleFunc("GET /hazards/{id}", hazardHandler.Get)
	mux.HandleFunc("POST /hazards", hazardHandler.Create)
	mux.HandleFunc("PUT /hazards/{id}", hazardHandler.Update)
	mux.HandleFunc("DELETE /hazards/{id}", hazardHandler.Delete)

	// Entities with foreign keys, ordered so their referenced tables
	// are already migrated.
	fieldStore, err := field.NewStore(db)
	if err != nil {
		return nil, err
	}
	fieldHandler := field.NewHandler(fieldStore)
	mux.HandleFunc("GET /fields", fieldHandler.List)
	mux.HandleFunc("GET /fields/{id}", fieldHandler.Get)
	mux.HandleFunc("POST /fields", fieldHandler.Create)
	mux.HandleFunc("PUT /fields/{id}", fieldHandler.Update)
	mux.HandleFunc("DELETE /fields/{id}", fieldHandler.Delete)

	fieldCoordinateStore, err := fieldcoordinate.NewStore(db)
	if err != nil {
		return nil, err
	}
	fieldCoordinateHandler := fieldcoordinate.NewHandler(fieldCoordinateStore)
	mux.HandleFunc("GET /field-coordinates", fieldCoordinateHandler.List)
	mux.HandleFunc("GET /field-coordinates/{id}", fieldCoordinateHandler.Get)
	mux.HandleFunc("POST /field-coordinates", fieldCoordinateHandler.Create)
	mux.HandleFunc("PUT /field-coordinates/{id}", fieldCoordinateHandler.Update)
	mux.HandleFunc("DELETE /field-coordinates/{id}", fieldCoordinateHandler.Delete)

	cropStore, err := crop.NewStore(db)
	if err != nil {
		return nil, err
	}
	cropHandler := crop.NewHandler(cropStore)
	mux.HandleFunc("GET /crops", cropHandler.List)
	mux.HandleFunc("GET /crops/{id}", cropHandler.Get)
	mux.HandleFunc("POST /crops", cropHandler.Create)
	mux.HandleFunc("PUT /crops/{id}", cropHandler.Update)
	mux.HandleFunc("DELETE /crops/{id}", cropHandler.Delete)

	cultivationRiskStore, err := cultivationrisk.NewStore(db)
	if err != nil {
		return nil, err
	}
	cultivationRiskHandler := cultivationrisk.NewHandler(cultivationRiskStore)
	mux.HandleFunc("GET /cultivation-risks", cultivationRiskHandler.List)
	mux.HandleFunc("GET /cultivation-risks/{id}", cultivationRiskHandler.Get)
	mux.HandleFunc("POST /cultivation-risks", cultivationRiskHandler.Create)
	mux.HandleFunc("PUT /cultivation-risks/{id}", cultivationRiskHandler.Update)
	mux.HandleFunc("DELETE /cultivation-risks/{id}", cultivationRiskHandler.Delete)

	cultivationGuidelineStore, err := cultivationguideline.NewStore(db)
	if err != nil {
		return nil, err
	}
	cultivationGuidelineHandler := cultivationguideline.NewHandler(cultivationGuidelineStore)
	mux.HandleFunc("GET /cultivation-guidelines", cultivationGuidelineHandler.List)
	mux.HandleFunc("GET /cultivation-guidelines/{id}", cultivationGuidelineHandler.Get)
	mux.HandleFunc("POST /cultivation-guidelines", cultivationGuidelineHandler.Create)
	mux.HandleFunc("PUT /cultivation-guidelines/{id}", cultivationGuidelineHandler.Update)
	mux.HandleFunc("DELETE /cultivation-guidelines/{id}", cultivationGuidelineHandler.Delete)

	incidentStore, err := incident.NewStore(db)
	if err != nil {
		return nil, err
	}
	incidentHandler := incident.NewHandler(incidentStore)
	mux.HandleFunc("GET /incidents", incidentHandler.List)
	mux.HandleFunc("GET /incidents/{id}", incidentHandler.Get)
	mux.HandleFunc("POST /incidents", incidentHandler.Create)
	mux.HandleFunc("PUT /incidents/{id}", incidentHandler.Update)
	mux.HandleFunc("DELETE /incidents/{id}", incidentHandler.Delete)

	alertStore, err := alert.NewStore(db)
	if err != nil {
		return nil, err
	}
	alertHandler := alert.NewHandler(alertStore)
	mux.HandleFunc("GET /alerts", alertHandler.List)
	mux.HandleFunc("GET /alerts/{id}", alertHandler.Get)
	mux.HandleFunc("POST /alerts", alertHandler.Create)
	mux.HandleFunc("PUT /alerts/{id}", alertHandler.Update)
	mux.HandleFunc("DELETE /alerts/{id}", alertHandler.Delete)

	userStore, err := user.NewStore(db)
	if err != nil {
		return nil, err
	}
	userHandler := user.NewHandler(userStore)
	mux.HandleFunc("GET /users", userHandler.List)
	mux.HandleFunc("GET /users/{id}", userHandler.Get)
	mux.HandleFunc("POST /users", userHandler.Create)
	mux.HandleFunc("PUT /users/{id}", userHandler.Update)
	mux.HandleFunc("DELETE /users/{id}", userHandler.Delete)

	return logRequests(mux), nil
}

func health(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.Write([]byte(`{"status":"ok"}`))
}
