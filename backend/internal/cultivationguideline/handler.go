package cultivationguideline

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
	guidelines, err := h.store.List(r.Context())
	if err != nil {
		http.Error(w, "failed to list cultivationguidelines", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(guidelines)
}

func (h *Handler) Get(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	guideline, err := h.store.GetByID(r.Context(), id)
	if err != nil {
		http.Error(w, "cultivationguideline not found", http.StatusNotFound)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(guideline)
}

func (h *Handler) Create(w http.ResponseWriter, r *http.Request) {
	var req CreateCultivationGuidelineRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	if err := validate.Struct(req); err != nil {
		utils.WriteValidationError(w, err)
		return
	}
	guideline, err := h.store.Create(r.Context(), req)
	if err != nil {
		http.Error(w, "failed to create cultivationguideline", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(guideline)
}

func (h *Handler) Update(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	var req UpdateCultivationGuidelineRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	if err := validate.Struct(req); err != nil {
		utils.WriteValidationError(w, err)
		return
	}
	guideline, err := h.store.Update(r.Context(), id, req)
	if err != nil {
		http.Error(w, "cultivationguideline not found", http.StatusNotFound)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(guideline)
}

func (h *Handler) Delete(w http.ResponseWriter, r *http.Request) {
	id, err := strconv.ParseInt(r.PathValue("id"), 10, 64)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	if err := h.store.Delete(r.Context(), id); err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			http.Error(w, "cultivationguideline not found", http.StatusNotFound)
			return
		}
		http.Error(w, "failed to delete cultivationguideline", http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}
