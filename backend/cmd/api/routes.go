package main

import (
	"muhfarming/internal/hazard"
	"muhfarming/internal/weather"
	"net/http"

	"gorm.io/gorm"
)

func setUpRoutes(db *gorm.DB) (http.Handler, error) {
	mux := http.NewServeMux()

	mux.HandleFunc("GET /health", health)

	weatherHandler := weather.NewHandler()
	mux.HandleFunc("GET /forecast", weatherHandler.Forecast)

	hazardStore, err := hazard.NewStore(db)
	if err != nil {
		return nil, err
	}
	hazardHandler := hazard.NewHandler(hazardStore)
	mux.HandleFunc("GET /hazards", hazardHandler.List)
	mux.HandleFunc("GET /hazards/{id}", hazardHandler.Get)
	mux.HandleFunc("POST /hazards", hazardHandler.Create)
	mux.HandleFunc("DELETE /hazards/{id}", hazardHandler.Delete)

	return logRequests(mux), nil
}

func health(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.Write([]byte(`{"status":"ok"}`))
}
