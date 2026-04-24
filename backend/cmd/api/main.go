package main

import (
	"log"
	"muhfarming/internal/handlers"
	"net/http"
)

func main() {
	log.Println("Server is starting.")
	startServer()
}

func startServer() error {
	h := handlers.New()
	mux := setUpRoutes(h)

	srv := &http.Server{
		Addr:    ":8080",
		Handler: mux,
	}

	srvErr := srv.ListenAndServe()
	if srvErr != nil {
		log.Fatal("Listen and Serve failed during start up: %w", srvErr)
	}
	return nil
}

func setUpRoutes(h *handlers.Handler) http.Handler {
	mux := http.NewServeMux()

	mux.HandleFunc("GET /health", h.Health)
	mux.HandleFunc("GET /forecast", h.Forecast)

	return mux
}
