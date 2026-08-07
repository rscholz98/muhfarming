package cultivationguideline

import (
	"muhfarming/internal/cultivation"
	"muhfarming/internal/fertilizer"

	"gorm.io/gorm"
)

type CultivationGuideline struct {
	gorm.Model
	Type         string `json:"type"`
	WeekFrom     int    `json:"weekFrom"`
	WeekTo       int    `json:"weekTo"`
	Instructions string `json:"instructions"`

	CultivationID uint                    `json:"cultivationId" gorm:"not null;index"`
	Cultivation   cultivation.Cultivation `json:"cultivation,omitempty"`

	FertilizerID *uint                  `json:"fertilizerId" gorm:"index"`
	Fertilizer   *fertilizer.Fertilizer `json:"fertilizer,omitempty"`
}

type CreateCultivationGuidelineRequest struct {
	Type          string `json:"type"`
	WeekFrom      int    `json:"weekFrom"`
	WeekTo        int    `json:"weekTo"`
	Instructions  string `json:"instructions"`
	CultivationID uint   `json:"cultivationId" validate:"required"`
	FertilizerID  *uint  `json:"fertilizerId"`
}

type UpdateCultivationGuidelineRequest struct {
	Type          string `json:"type"`
	WeekFrom      int    `json:"weekFrom"`
	WeekTo        int    `json:"weekTo"`
	Instructions  string `json:"instructions"`
	CultivationID uint   `json:"cultivationId" validate:"required"`
	FertilizerID  *uint  `json:"fertilizerId"`
}
