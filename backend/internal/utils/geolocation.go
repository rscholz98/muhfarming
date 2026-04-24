package utils

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
)

type Location struct {
	Longitude float32
	Latitude  float32
}

// IPAPIResponse represents the minimal response we need from ip-api.com
type IPAPIResponse struct {
	Status string  `json:"status"`
	Lat    float32 `json:"lat"`
	Lon    float32 `json:"lon"`
}

func GetLocation(ip string) (*Location, error) {
	url := "http://ip-api.com/json/" + ip

	req, reqErr := http.NewRequest("GET", url, nil)
	if reqErr != nil {
		return nil, fmt.Errorf("request creation failed: %w", reqErr)
	}

	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		return nil, fmt.Errorf("request failed: %w", respErr)
	}
	defer resp.Body.Close()

	body, readErr := io.ReadAll(resp.Body)
	if readErr != nil {
		return nil, fmt.Errorf("failed to read response body: %w", readErr)
	}

	var apiResponse IPAPIResponse
	if err := json.Unmarshal(body, &apiResponse); err != nil {
		return nil, fmt.Errorf("failed to parse JSON response: %w", err)
	}

	if apiResponse.Status != "success" {
		return nil, fmt.Errorf("IP API returned status: %s", apiResponse.Status)
	}

	// Extract only lat and lon to Location
	location := &Location{
		Latitude:  apiResponse.Lat,
		Longitude: apiResponse.Lon,
	}

	return location, nil
}
