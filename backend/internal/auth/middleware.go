package auth

import (
	"net/http"
	"strings"
)

// RequireAuth wraps a handler, rejecting requests without a valid bearer token
// and injecting the caller identity into the request context.
func (t *TokenService) RequireAuth(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		header := r.Header.Get("Authorization")
		const prefix = "Bearer "
		if !strings.HasPrefix(header, prefix) {
			http.Error(w, "missing or malformed Authorization header", http.StatusUnauthorized)
			return
		}
		id, err := t.Verify(strings.TrimPrefix(header, prefix))
		if err != nil {
			http.Error(w, "invalid or expired token", http.StatusUnauthorized)
			return
		}
		next.ServeHTTP(w, r.WithContext(WithIdentity(r.Context(), id)))
	})
}

// RequireAdmin wraps a handler so only admins may proceed. It must be used
// inside RequireAuth (which populates the identity).
func RequireAdmin(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		id, ok := FromContext(r.Context())
		if !ok || !id.IsAdmin() {
			http.Error(w, "admin privileges required", http.StatusForbidden)
			return
		}
		next(w, r)
	}
}
