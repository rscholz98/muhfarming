package user

import "gorm.io/gorm"

type User struct {
	gorm.Model
	Username     string `json:"username" gorm:"uniqueIndex;not null"`
	PasswordHash string `json:"-" gorm:"not null"`
	Name         string `json:"name" gorm:"not null"`
	Surname      string `json:"surname"`
	Title        string `json:"title"`
	Email        string `json:"email" gorm:"not null"`
	Language     string `json:"language"`
	Role         string `json:"role" gorm:"not null"`
}

// CreateUserRequest is used by admins to create users. Regular self-service
// registration goes through the auth package's signup flow instead.
type CreateUserRequest struct {
	Username string `json:"username" validate:"required"`
	Password string `json:"password" validate:"required"`
	Name     string `json:"name" validate:"required"`
	Surname  string `json:"surname"`
	Title    string `json:"title"`
	Email    string `json:"email" validate:"required"`
	Language string `json:"language"`
	Role     string `json:"role" validate:"oneof=Admin Farmer"`
}

// UpdateUserRequest is used by admins to update users. Password is optional on
// update; when empty the existing hash is left unchanged.
type UpdateUserRequest struct {
	Password string `json:"password"`
	Name     string `json:"name" validate:"required"`
	Surname  string `json:"surname"`
	Title    string `json:"title"`
	Email    string `json:"email" validate:"required"`
	Language string `json:"language"`
	Role     string `json:"role" validate:"oneof=Admin Farmer"`
}
