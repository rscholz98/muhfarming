package handlers

import (
	"fmt"
	"io"
	"muhfarming/internal/utils"
	"net/http"
	"strconv"
)

func (h *Handler) Forecast(w http.ResponseWriter, r *http.Request) {
	// Parse query parameters
	query := r.URL.Query()

	ipAddress := query.Get("ip_address")
	if ipAddress == "" {
		http.Error(w, "IP address is required", http.StatusBadRequest)
		return
	}

	startDate := query.Get("start_date")
	endDate := query.Get("end_date")
	forecastDaysStr := query.Get("forecast_days")

	// Get location from IP address
	loc, locErr := utils.GetLocation(ipAddress)
	if locErr != nil {
		http.Error(w, "Failed to get location: "+locErr.Error(), http.StatusInternalServerError)
		return
	}

	// Build Open-Meteo URL based on request parameters
	var meteoUrl string

	if startDate != "" && endDate != "" {
		// Use start and end dates
		meteoUrl = fmt.Sprintf("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&hourly=temperature_2m&start_date=%s&end_date=%s",
			loc.Latitude, loc.Longitude, startDate, endDate)
	} else {
		// Use forecast days (default 16 if not provided)
		forecastDays := 16
		if forecastDaysStr != "" {
			if days, err := strconv.Atoi(forecastDaysStr); err == nil {
				forecastDays = days
			}
		}
		meteoUrl = fmt.Sprintf("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&hourly=temperature_2m&forecast_days=%d",
			loc.Latitude, loc.Longitude, forecastDays)
	}

	// Make API call to Open-Meteo
	resp, err := http.Get(meteoUrl)
	if err != nil {
		http.Error(w, "Failed to fetch weather data: "+err.Error(), http.StatusInternalServerError)
		return
	}
	defer resp.Body.Close()

	// Read and return the weather data
	weatherData, err := io.ReadAll(resp.Body)
	if err != nil {
		http.Error(w, "Failed to read weather response", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.Write(weatherData)
}
