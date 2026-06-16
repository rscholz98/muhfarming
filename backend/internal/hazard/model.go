package hazard

import "gorm.io/gorm"

type Hazard struct {
	gorm.Model
	Name        string `json:"name"        gorm:"not null"`
	Description string `json:"description" gorm:"not null"`
}

type CreateHazardRequest struct {
	Name        string `json:"name"        validate:"required"`
	Description string `json:"description" validate:"required"`
}
