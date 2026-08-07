package region

import "gorm.io/gorm"

type Region struct {
	gorm.Model
	Name    string `json:"name"    gorm:"not null"`
	GeoCode string `json:"geoCode"`
}

type CreateRegionRequest struct {
	Name    string `json:"name"    validate:"required"`
	GeoCode string `json:"geoCode"`
}

type UpdateRegionRequest struct {
	Name    string `json:"name"    validate:"required"`
	GeoCode string `json:"geoCode"`
}
