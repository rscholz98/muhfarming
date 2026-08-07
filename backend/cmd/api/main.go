package main

import (
	"context"
	"log"
	"muhfarming/internal/auth"
	"muhfarming/internal/db"
	"muhfarming/internal/user"
	"net/http"
	"os"
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

	// JWT signing secret is required — refuse to start without it so we never
	// silently issue tokens signed with an empty/guessable key.
	secret := os.Getenv("JWT_SECRET")
	if secret == "" {
		return errMissingEnv("JWT_SECRET")
	}
	tokens := auth.NewTokenService(secret)

	// The user store is shared between the auth handlers (signup/login) and the
	// admin user-management routes, and is used to seed the admin below.
	userStore, err := user.NewStore(gormDB)
	if err != nil {
		return err
	}

	// Seed the admin user from environment variables (idempotent).
	adminUser := os.Getenv("ADMIN_USERNAME")
	adminPass := os.Getenv("ADMIN_PASSWORD")
	if adminUser != "" && adminPass != "" {
		if err := userStore.EnsureAdmin(context.Background(), adminUser, adminPass); err != nil {
			return err
		}
		log.Printf("Admin user '%s' ensured.", adminUser)
	} else {
		log.Printf("ADMIN_USERNAME/ADMIN_PASSWORD not set — skipping admin seed.")
	}

	mux, err := setUpRoutes(gormDB, tokens, userStore)
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

type errMissingEnv string

func (e errMissingEnv) Error() string {
	return "required environment variable not set: " + string(e)
}
