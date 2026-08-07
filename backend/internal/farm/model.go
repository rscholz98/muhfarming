package farm

import "gorm.io/gorm"

type Farm struct {
	gorm.Model
	Name string `json:"name" gorm:"not null"`
}

type CreateFarmRequest struct {
	Name string `json:"name" validate:"required"`
}

type UpdateFarmRequest struct {
	Name string `json:"name" validate:"required"`
}
