package weather

import (
	"encoding/json"
	"muhfarming/internal/utils"
	"net/http"

	"github.com/go-playground/validator/v10"
)

var validate = validator.New()

type Handler struct{}

func NewHandler() *Handler {
	return &Handler{}
}

func (h *Handler) Forecast(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()
	req := ForecastRequest{
		IPAddress: query.Get("ip_address"),
		StartDate: query.Get("start_date"),
		EndDate:   query.Get("end_date"),
	}

	if err := validate.Struct(req); err != nil {
		utils.WriteValidationError(w, err)
		return
	}

	loc, err := utils.Lookup(req.IPAddress)
	if err != nil {
		http.Error(w, "failed to get location: "+err.Error(), http.StatusInternalServerError)
		return
	}

	forecast, err := fetchForecast(loc.Latitude, loc.Longitude, req.StartDate, req.EndDate)
	if err != nil {
		http.Error(w, "failed to fetch weather data: "+err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(forecast)
}
