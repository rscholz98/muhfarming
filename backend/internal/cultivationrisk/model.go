package cultivationrisk

import (
	"muhfarming/internal/cultivation"
	"muhfarming/internal/hazard"

	"gorm.io/gorm"
)

type CultivationRisk struct {
	gorm.Model
	WeekFrom int    `json:"weekFrom"`
	WeekTo   int    `json:"weekTo"`
	Solution string `json:"solution"`

	CultivationID uint                    `json:"cultivationId" gorm:"not null;index"`
	Cultivation   cultivation.Cultivation `json:"cultivation,omitempty"`

	HazardID uint          `json:"hazardId" gorm:"not null;index"`
	Hazard   hazard.Hazard `json:"hazard,omitempty"`
}

type CreateCultivationRiskRequest struct {
	WeekFrom      int    `json:"weekFrom"`
	WeekTo        int    `json:"weekTo"`
	Solution      string `json:"solution"`
	CultivationID uint   `json:"cultivationId" validate:"required"`
	HazardID      uint   `json:"hazardId" validate:"required"`
}

type UpdateCultivationRiskRequest struct {
	WeekFrom      int    `json:"weekFrom"`
	WeekTo        int    `json:"weekTo"`
	Solution      string `json:"solution"`
	CultivationID uint   `json:"cultivationId" validate:"required"`
	HazardID      uint   `json:"hazardId" validate:"required"`
}
