package auth

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"

	"muhfarming/internal/utils"

	"github.com/go-playground/validator/v10"
)

var validate = validator.New()

// UserRecord is the minimal view of a user that the auth handlers need.
type UserRecord struct {
	ID           uint
	Username     string
	PasswordHash string
	Role         string
}

// UserStore is the subset of user-store behavior the auth handlers depend on.
// The concrete user store satisfies this via a thin adapter (wired in routes).
type UserStore interface {
	// CreateFarmer registers a new farmer with the given plaintext password
	// (the implementation is responsible for hashing).
	CreateFarmer(ctx context.Context, username, password string) (UserRecord, error)
	// FindByUsername returns the stored user record for login verification.
	FindByUsername(ctx context.Context, username string) (UserRecord, error)
}

type Handler struct {
	users  UserStore
	tokens *TokenService
}

func NewHandler(users UserStore, tokens *TokenService) *Handler {
	return &Handler{users: users, tokens: tokens}
}

type credentials struct {
	Username string `json:"username" validate:"required"`
	Password string `json:"password" validate:"required,min=6"`
}

type tokenResponse struct {
	Token string `json:"token"`
	Role  string `json:"role"`
}

// Signup registers a new farmer and returns a JWT.
func (h *Handler) Signup(w http.ResponseWriter, r *http.Request) {
	var req credentials
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	if err := validate.Struct(req); err != nil {
		utils.WriteValidationError(w, err)
		return
	}
	rec, err := h.users.CreateFarmer(r.Context(), req.Username, req.Password)
	if err != nil {
		// Most likely a duplicate username (unique index).
		http.Error(w, "username already taken", http.StatusConflict)
		return
	}
	h.issue(w, rec, http.StatusCreated)
}

// Login verifies credentials and returns a JWT.
func (h *Handler) Login(w http.ResponseWriter, r *http.Request) {
	var req credentials
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	rec, err := h.users.FindByUsername(r.Context(), req.Username)
	if err != nil || !CheckPassword(rec.PasswordHash, req.Password) {
		// Same response for unknown user and wrong password (no enumeration).
		http.Error(w, "invalid username or password", http.StatusUnauthorized)
		return
	}
	h.issue(w, rec, http.StatusOK)
}

func (h *Handler) issue(w http.ResponseWriter, rec UserRecord, status int) {
	token, err := h.tokens.Issue(rec.ID, rec.Role)
	if err != nil {
		http.Error(w, "failed to issue token", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(tokenResponse{Token: token, Role: rec.Role})
}

// ErrNotImplemented is a sentinel for adapters that don't support an operation.
var ErrNotImplemented = errors.New("not implemented")
