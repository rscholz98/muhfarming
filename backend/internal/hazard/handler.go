package hazard

import (
	"encoding/json"
	"errors"
	"muhfarming/internal/utils"
	"net/http"
	"strconv"

	"github.com/go-playground/validator/v10"
	"gorm.io/gorm"
)

var validate = validator.New()

type Handler struct {
	store store
}

func NewHandler(s store) *Handler {
	return &Handler{store: s}
}

func (h *Handler) List(w http.ResponseWriter, r *http.Request) {
	hazards, err := h.store.List(r.Context())
	if err != nil {
		http.Error(w, "failed to list hazards", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(hazards)
}

func (h *Handler) Get(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	hazard, err := h.store.GetByID(r.Context(), id)
	if err != nil {
		http.Error(w, "hazard not found", http.StatusNotFound)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(hazard)
}

func (h *Handler) Create(w http.ResponseWriter, r *http.Request) {
	var req CreateHazardRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	if err := validate.Struct(req); err != nil {
		utils.WriteValidationError(w, err)
		return
	}
	hazard, err := h.store.Create(r.Context(), req)
	if err != nil {
		http.Error(w, "failed to create hazard", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(hazard)
}

func (h *Handler) Update(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	var req UpdateHazardRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	if err := validate.Struct(req); err != nil {
		utils.WriteValidationError(w, err)
		return
	}
	hazard, err := h.store.Update(r.Context(), id, req)
	if err != nil {
		http.Error(w, "hazard not found", http.StatusNotFound)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(hazard)
}

func (h *Handler) Delete(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	if err := h.store.Delete(r.Context(), id); err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			http.Error(w, "hazard not found", http.StatusNotFound)
			return
		}
		http.Error(w, "failed to delete hazard", http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}
