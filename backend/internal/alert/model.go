package alert

import (
	"muhfarming/internal/field"
	"muhfarming/internal/incident"

	"gorm.io/gorm"
)

type Alert struct {
	gorm.Model
	FieldID    uint              `json:"fieldId" gorm:"not null;index"`
	Field      field.Field       `json:"field,omitempty"`
	IncidentID uint              `json:"incidentId" gorm:"not null;index"`
	Incident   incident.Incident `json:"incident,omitempty"`
}

type CreateAlertRequest struct {
	FieldID    uint `json:"fieldId" validate:"required"`
	IncidentID uint `json:"incidentId" validate:"required"`
}

type UpdateAlertRequest struct {
	FieldID    uint `json:"fieldId" validate:"required"`
	IncidentID uint `json:"incidentId" validate:"required"`
}
