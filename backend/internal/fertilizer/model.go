package fertilizer

import "gorm.io/gorm"

type Fertilizer struct {
	gorm.Model
	Name string `json:"name" gorm:"not null"`
}

type CreateFertilizerRequest struct {
	Name string `json:"name" validate:"required"`
}

type UpdateFertilizerRequest struct {
	Name string `json:"name" validate:"required"`
}
