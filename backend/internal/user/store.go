package user

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&User{}); err != nil {
		return nil, fmt.Errorf("migrate user: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*User, error)
	List(ctx context.Context) ([]User, error)
	Create(ctx context.Context, req CreateUserRequest) (*User, error)
	Update(ctx context.Context, id int64, req UpdateUserRequest) (*User, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*User, error) {
	var u User
	if err := s.db.WithContext(ctx).Preload("Farm").First(&u, id).Error; err != nil {
		return nil, fmt.Errorf("user not found: %w", err)
	}
	return &u, nil
}

func (s *Store) List(ctx context.Context) ([]User, error) {
	var users []User
	if err := s.db.WithContext(ctx).Preload("Farm").Find(&users).Error; err != nil {
		return nil, err
	}
	return users, nil
}

func (s *Store) Create(ctx context.Context, req CreateUserRequest) (*User, error) {
	u := User{
		Name:     req.Name,
		Surname:  req.Surname,
		Title:    req.Title,
		Email:    req.Email,
		Language: req.Language,
		Role:     req.Role,
		FarmID:   req.FarmID,
	}
	if err := s.db.WithContext(ctx).Create(&u).Error; err != nil {
		return nil, fmt.Errorf("create user failed: %w", err)
	}
	return &u, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateUserRequest) (*User, error) {
	var u User
	if err := s.db.WithContext(ctx).First(&u, id).Error; err != nil {
		return nil, fmt.Errorf("user not found: %w", err)
	}
	u.Name = req.Name
	u.Surname = req.Surname
	u.Title = req.Title
	u.Email = req.Email
	u.Language = req.Language
	u.Role = req.Role
	u.FarmID = req.FarmID
	if err := s.db.WithContext(ctx).Save(&u).Error; err != nil {
		return nil, fmt.Errorf("update user failed: %w", err)
	}
	return &u, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&User{}, id).Error
}
