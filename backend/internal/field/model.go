package field

import (
	"muhfarming/internal/farm"
	"muhfarming/internal/region"

	"gorm.io/gorm"
)

type Field struct {
	gorm.Model
	Name       string        `json:"name" gorm:"not null"`
	FieldNotes string        `json:"fieldNotes"`
	FarmID     uint          `json:"farmId" gorm:"not null;index"`
	Farm       farm.Farm     `json:"farm,omitempty"`
	RegionID   uint          `json:"regionId" gorm:"not null;index"`
	Region     region.Region `json:"region,omitempty"`
}

type CreateFieldRequest struct {
	Name       string `json:"name" validate:"required"`
	FieldNotes string `json:"fieldNotes"`
	FarmID     uint   `json:"farmId" validate:"required"`
	RegionID   uint   `json:"regionId" validate:"required"`
}

type UpdateFieldRequest struct {
	Name       string `json:"name" validate:"required"`
	FieldNotes string `json:"fieldNotes"`
	FarmID     uint   `json:"farmId" validate:"required"`
	RegionID   uint   `json:"regionId" validate:"required"`
}
