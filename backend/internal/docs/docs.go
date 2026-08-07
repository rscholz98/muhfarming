package docs

import (
	_ "embed"
	"net/http"
)

//go:embed openapi.yaml
var openAPISpec []byte

// swaggerPage renders the interactive API reference using Swagger UI, loaded
// from a CDN, pointed at the spec served by this package at /openapi.yaml.
// Unlike Redoc, Swagger UI provides a "Try it out" button that sends real
// requests to the server.
const swaggerPage = `<!DOCTYPE html>
<html>
  <head>
    <title>Muhfarming API</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css" />
    <style>body { margin: 0; padding: 0; }</style>
  </head>
  <body>
    <div id="swagger-ui"></div>
    <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
    <script>
      window.onload = function () {
        window.ui = SwaggerUIBundle({
          url: "/openapi.yaml",
          dom_id: "#swagger-ui",
        });
      };
    </script>
  </body>
</html>`

type Handler struct{}

func NewHandler() *Handler {
	return &Handler{}
}

// Spec serves the raw OpenAPI specification.
func (h *Handler) Spec(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "application/yaml")
	w.Write(openAPISpec)
}

// UI serves the Swagger UI documentation page. It is registered on the root
// pattern ("GET /"), which the standard-library mux treats as a catch-all, so
// requests for any unknown path fall through here — guard against that and
// return 404 for anything other than the exact root.
func (h *Handler) UI(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/" {
		http.NotFound(w, r)
		return
	}
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.Write([]byte(swaggerPage))
}
