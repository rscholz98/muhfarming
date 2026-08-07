package user

import (
	"muhfarming/internal/farm"

	"gorm.io/gorm"
)

type User struct {
	gorm.Model
	Name     string `json:"name" gorm:"not null"`
	Surname  string `json:"surname"`
	Title    string `json:"title"`
	Email    string `json:"email" gorm:"not null"`
	Language string `json:"language"`
	Role     string `json:"role" gorm:"not null"`

	FarmID uint      `json:"farmId" gorm:"not null;index"`
	Farm   farm.Farm `json:"farm,omitempty"`
}

type CreateUserRequest struct {
	Name     string `json:"name" validate:"required"`
	Surname  string `json:"surname"`
	Title    string `json:"title"`
	Email    string `json:"email" validate:"required"`
	Language string `json:"language"`
	Role     string `json:"role" validate:"oneof=Admin Farmer"`
	FarmID   uint   `json:"farmId" validate:"required"`
}

type UpdateUserRequest struct {
	Name     string `json:"name" validate:"required"`
	Surname  string `json:"surname"`
	Title    string `json:"title"`
	Email    string `json:"email" validate:"required"`
	Language string `json:"language"`
	Role     string `json:"role" validate:"oneof=Admin Farmer"`
	FarmID   uint   `json:"farmId" validate:"required"`
}
