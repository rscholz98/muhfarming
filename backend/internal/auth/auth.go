// Package auth provides authentication (password hashing, JWT issuance and
// verification) and request-scoped identity for the muhfarming API.
package auth

import (
	"context"

	"golang.org/x/crypto/bcrypt"
)

// Role values stored on the user and carried in the JWT.
const (
	RoleAdmin  = "Admin"
	RoleFarmer = "Farmer"
)

// Identity is the authenticated caller, extracted from a verified JWT and
// carried on the request context.
type Identity struct {
	UserID uint
	Role   string
}

// IsAdmin reports whether the identity has the admin role.
func (i Identity) IsAdmin() bool { return i.Role == RoleAdmin }

type contextKey struct{}

var identityKey = contextKey{}

// WithIdentity returns a copy of ctx carrying the given identity.
func WithIdentity(ctx context.Context, id Identity) context.Context {
	return context.WithValue(ctx, identityKey, id)
}

// FromContext returns the identity stored on ctx, and whether one was present.
func FromContext(ctx context.Context) (Identity, bool) {
	id, ok := ctx.Value(identityKey).(Identity)
	return id, ok
}

// HashPassword returns the bcrypt hash of a plaintext password.
func HashPassword(plaintext string) (string, error) {
	hash, err := bcrypt.GenerateFromPassword([]byte(plaintext), bcrypt.DefaultCost)
	if err != nil {
		return "", err
	}
	return string(hash), nil
}

// CheckPassword reports whether plaintext matches the stored bcrypt hash.
func CheckPassword(hash, plaintext string) bool {
	return bcrypt.CompareHashAndPassword([]byte(hash), []byte(plaintext)) == nil
}
