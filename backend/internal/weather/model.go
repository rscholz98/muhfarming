package weather

type ForecastRequest struct {
	IPAddress string `validate:"required,ip"`
	StartDate string `validate:"required,datetime=2006-01-02"`
	EndDate   string `validate:"required,datetime=2006-01-02"`
}

type ForecastResponse struct {
	Latitude             float64     `json:"latitude"`
	Longitude            float64     `json:"longitude"`
	GenerationtimeMs     float64     `json:"generationtime_ms"`
	UtcOffsetSeconds     int         `json:"utc_offset_seconds"`
	Timezone             string      `json:"timezone"`
	TimezoneAbbreviation string      `json:"timezone_abbreviation"`
	Elevation            float64     `json:"elevation"`
	HourlyUnits          HourlyUnits `json:"hourly_units"`
	Hourly               HourlyData  `json:"hourly"`
}

type HourlyUnits struct {
	Time          string `json:"time"`
	Temperature2m string `json:"temperature_2m"`
}

type HourlyData struct {
	Time          []string  `json:"time"`
	Temperature2m []float64 `json:"temperature_2m"`
}
