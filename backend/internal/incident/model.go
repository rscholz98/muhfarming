package incident

import (
	"muhfarming/internal/cultivationrisk"
	"muhfarming/internal/region"

	"gorm.io/gorm"
)

type Incident struct {
	gorm.Model
	Date        string `json:"date"`
	Priority    string `json:"priority"`
	Description string `json:"description"`

	CultivationRiskID uint                            `json:"cultivationRiskId" gorm:"not null;index"`
	CultivationRisk   cultivationrisk.CultivationRisk `json:"cultivationRisk,omitempty"`

	RegionID uint          `json:"regionId" gorm:"not null;index"`
	Region   region.Region `json:"region,omitempty"`
}

type CreateIncidentRequest struct {
	Date              string `json:"date"`
	Priority          string `json:"priority"`
	Description       string `json:"description"`
	CultivationRiskID uint   `json:"cultivationRiskId" validate:"required"`
	RegionID          uint   `json:"regionId" validate:"required"`
}

type UpdateIncidentRequest struct {
	Date              string `json:"date"`
	Priority          string `json:"priority"`
	Description       string `json:"description"`
	CultivationRiskID uint   `json:"cultivationRiskId" validate:"required"`
	RegionID          uint   `json:"regionId" validate:"required"`
}
