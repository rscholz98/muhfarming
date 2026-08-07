package crop

import (
	"muhfarming/internal/cultivation"
	"muhfarming/internal/field"

	"gorm.io/gorm"
)

type Crop struct {
	gorm.Model
	Status        string                  `json:"status"`
	DatePlanted   string                  `json:"datePlanted"`
	LastUpdated   string                  `json:"lastUpdated"`
	FieldID       uint                    `json:"fieldId" gorm:"not null;index"`
	Field         field.Field             `json:"field,omitempty"`
	CultivationID uint                    `json:"cultivationId" gorm:"not null;index"`
	Cultivation   cultivation.Cultivation `json:"cultivation,omitempty"`
}

type CreateCropRequest struct {
	Status        string `json:"status"`
	DatePlanted   string `json:"datePlanted"`
	LastUpdated   string `json:"lastUpdated"`
	FieldID       uint   `json:"fieldId" validate:"required"`
	CultivationID uint   `json:"cultivationId" validate:"required"`
}

type UpdateCropRequest struct {
	Status        string `json:"status"`
	DatePlanted   string `json:"datePlanted"`
	LastUpdated   string `json:"lastUpdated"`
	FieldID       uint   `json:"fieldId" validate:"required"`
	CultivationID uint   `json:"cultivationId" validate:"required"`
}
