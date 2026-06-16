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

type ipAPIResponse struct {
	Status string  `json:"status"`
	Lat    float32 `json:"lat"`
	Lon    float32 `json:"lon"`
}

func Lookup(ip string) (*Location, error) {
	url := "http://ip-api.com/json/" + ip

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("request creation failed: %w", err)
	}

	resp, err := Client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("request failed: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("read response body: %w", err)
	}

	var apiResponse ipAPIResponse
	if err := json.Unmarshal(body, &apiResponse); err != nil {
		return nil, fmt.Errorf("parse response: %w", err)
	}

	if apiResponse.Status != "success" {
		return nil, fmt.Errorf("ip-api returned status: %s", apiResponse.Status)
	}

	return &Location{
		Latitude:  apiResponse.Lat,
		Longitude: apiResponse.Lon,
	}, nil
}
