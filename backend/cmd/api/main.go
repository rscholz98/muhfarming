package main

import (
	"log"
	"muhfarming/internal/db"
	"net/http"
)

func main() {
	if err := startServer(); err != nil {
		log.Fatalf("Server failed: %v", err)
	}
}

const addr = ":8080"

func startServer() error {
	gormDB, err := db.Connect()
	if err != nil {
		return err
	}

	sqlDB, err := gormDB.DB()
	if err != nil {
		return err
	}
	defer sqlDB.Close()

	mux, err := setUpRoutes(gormDB)
	if err != nil {
		return err
	}

	srv := &http.Server{
		Addr:    addr,
		Handler: mux,
	}

	log.Printf("Server listening on port '%s'.", addr)
	log.Printf("Call root path for accessing Swagger UI.")
	return srv.ListenAndServe()
}
