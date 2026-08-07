package cultivation

import "gorm.io/gorm"

type Cultivation struct {
	gorm.Model
	Name                  string `json:"name"                   gorm:"not null"`
	EstTimeToHarvestWeeks int    `json:"estTimeToHarvestWeeks"`
}

type CreateCultivationRequest struct {
	Name                  string `json:"name"                  validate:"required"`
	EstTimeToHarvestWeeks int    `json:"estTimeToHarvestWeeks"`
}

type UpdateCultivationRequest struct {
	Name                  string `json:"name"                  validate:"required"`
	EstTimeToHarvestWeeks int    `json:"estTimeToHarvestWeeks"`
}
