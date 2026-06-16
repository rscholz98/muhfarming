package weather

import (
	"encoding/json"
	"fmt"
	"muhfarming/internal/utils"
)

func fetchForecast(latitude, longitude float32, startDate, endDate string) (*ForecastResponse, error) {
	url := fmt.Sprintf(
		"https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&hourly=temperature_2m&start_date=%s&end_date=%s",
		latitude, longitude, startDate, endDate,
	)

	resp, err := utils.Client.Get(url)
	if err != nil {
		return nil, fmt.Errorf("fetch forecast: %w", err)
	}
	defer resp.Body.Close()

	var forecast ForecastResponse
	if err := json.NewDecoder(resp.Body).Decode(&forecast); err != nil {
		return nil, fmt.Errorf("decode forecast: %w", err)
	}
	return &forecast, nil
}
