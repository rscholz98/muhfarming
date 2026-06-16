package utils

import (
	"encoding/json"
	"net/http"

	"github.com/go-playground/validator/v10"
)

type ValidationErrorResponse struct {
	Errors []FieldError `json:"errors"`
}

type FieldError struct {
	Field   string `json:"field"`
	Message string `json:"message"`
}

func WriteValidationError(w http.ResponseWriter, err error) {
	var fieldErrors []FieldError

	for _, e := range err.(validator.ValidationErrors) {
		fieldErrors = append(fieldErrors, FieldError{
			Field:   e.Field(),
			Message: validationMessage(e),
		})
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadRequest)
	json.NewEncoder(w).Encode(ValidationErrorResponse{Errors: fieldErrors})
}

func validationMessage(e validator.FieldError) string {
	switch e.Tag() {
	case "required":
		return "field is required"
	case "ip":
		return "must be a valid IP address"
	case "datetime":
		return "must be in " + e.Param() + " format"
	case "min":
		return "must be at least " + e.Param()
	case "max":
		return "must be at most " + e.Param()
	case "oneof":
		return "must be one of: " + e.Param()
	default:
		return "invalid value"
	}
}
