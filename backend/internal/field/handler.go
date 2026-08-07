package field

import (
	"encoding/json"
	"muhfarming/internal/utils"
	"net/http"
	"strconv"

	"github.com/go-playground/validator/v10"
)

var validate = validator.New()

type Handler struct {
	store store
}

func NewHandler(s store) *Handler {
	return &Handler{store: s}
}

func (h *Handler) List(w http.ResponseWriter, r *http.Request) {
	fields, err := h.store.List(r.Context())
	if err != nil {
		http.Error(w, "failed to list fields", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(fields)
}

func (h *Handler) Get(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	field, err := h.store.GetByID(r.Context(), id)
	if err != nil {
		http.Error(w, "field not found", http.StatusNotFound)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(field)
}

func (h *Handler) Create(w http.ResponseWriter, r *http.Request) {
	var req CreateFieldRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	if err := validate.Struct(req); err != nil {
		utils.WriteValidationError(w, err)
		return
	}
	field, err := h.store.Create(r.Context(), req)
	if err != nil {
		http.Error(w, "failed to create field", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(field)
}

func (h *Handler) Update(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	var req UpdateFieldRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	if err := validate.Struct(req); err != nil {
		utils.WriteValidationError(w, err)
		return
	}
	field, err := h.store.Update(r.Context(), id, req)
	if err != nil {
		http.Error(w, "field not found", http.StatusNotFound)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(field)
}

func (h *Handler) Delete(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	if err := h.store.Delete(r.Context(), id); err != nil {
		http.Error(w, "failed to delete field", http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}
