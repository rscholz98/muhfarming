package fieldcoordinate

import (
	"muhfarming/internal/field"

	"gorm.io/gorm"
)

type FieldCoordinate struct {
	gorm.Model
	Latitude      float64     `json:"latitude" gorm:"not null"`
	Longitude     float64     `json:"longitude" gorm:"not null"`
	SequenceOrder int         `json:"sequenceOrder"`
	FieldID       uint        `json:"fieldId" gorm:"not null;index"`
	Field         field.Field `json:"field,omitempty"`
}

type CreateFieldCoordinateRequest struct {
	Latitude      float64 `json:"latitude" validate:"required"`
	Longitude     float64 `json:"longitude" validate:"required"`
	SequenceOrder int     `json:"sequenceOrder"`
	FieldID       uint    `json:"fieldId" validate:"required"`
}

type UpdateFieldCoordinateRequest struct {
	Latitude      float64 `json:"latitude" validate:"required"`
	Longitude     float64 `json:"longitude" validate:"required"`
	SequenceOrder int     `json:"sequenceOrder"`
	FieldID       uint    `json:"fieldId" validate:"required"`
}
