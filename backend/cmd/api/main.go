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
		Addr:    ":8080",
		Handler: mux,
	}

	log.Println("Server is ready.")
	return srv.ListenAndServe()
}
