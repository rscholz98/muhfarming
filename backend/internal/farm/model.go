package farm

import "gorm.io/gorm"

type Farm struct {
	gorm.Model
	Name string `json:"name" gorm:"not null"`
	// UserID is the owning user. Set from the authenticated caller on create;
	// used to scope which farms a farmer can see.
	UserID uint `json:"userId" gorm:"not null;index"`
}

type CreateFarmRequest struct {
	Name string `json:"name" validate:"required"`
}

type UpdateFarmRequest struct {
	Name string `json:"name" validate:"required"`
}
