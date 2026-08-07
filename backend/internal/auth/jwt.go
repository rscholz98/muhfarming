package auth

import (
	"errors"
	"fmt"
	"strconv"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

// tokenTTL is how long an issued token stays valid.
const tokenTTL = 24 * time.Hour

// TokenService issues and verifies HS256 JWTs signed with a shared secret.
type TokenService struct {
	secret []byte
}

// NewTokenService returns a token service using the given signing secret.
func NewTokenService(secret string) *TokenService {
	return &TokenService{secret: []byte(secret)}
}

// Issue creates a signed token for the given user id and role. The token
// intentionally does NOT carry farm ids — a user may own many farms, so data
// scoping is derived from the database per request rather than from the token.
func (t *TokenService) Issue(userID uint, role string) (string, error) {
	now := time.Now()
	claims := jwt.RegisteredClaims{
		Subject:   strconv.FormatUint(uint64(userID), 10),
		IssuedAt:  jwt.NewNumericDate(now),
		ExpiresAt: jwt.NewNumericDate(now.Add(tokenTTL)),
	}
	// Role is a private claim alongside the registered ones.
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"sub":  claims.Subject,
		"role": role,
		"iat":  claims.IssuedAt.Unix(),
		"exp":  claims.ExpiresAt.Unix(),
	})
	return token.SignedString(t.secret)
}

// Verify parses and validates a token, returning the caller identity.
func (t *TokenService) Verify(tokenString string) (Identity, error) {
	token, err := jwt.Parse(tokenString, func(tok *jwt.Token) (any, error) {
		if _, ok := tok.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", tok.Header["alg"])
		}
		return t.secret, nil
	})
	if err != nil {
		return Identity{}, err
	}
	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok || !token.Valid {
		return Identity{}, errors.New("invalid token")
	}
	sub, ok := claims["sub"].(string)
	if !ok {
		return Identity{}, errors.New("token missing subject")
	}
	uid, err := strconv.ParseUint(sub, 10, 64)
	if err != nil {
		return Identity{}, errors.New("token subject not a valid id")
	}
	role, _ := claims["role"].(string)
	return Identity{UserID: uint(uid), Role: role}, nil
}
