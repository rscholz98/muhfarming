package hazard

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Hazard{}); err != nil {
		return nil, fmt.Errorf("migrate hazard: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Hazard, error)
	List(ctx context.Context) ([]Hazard, error)
	Create(ctx context.Context, req CreateHazardRequest) (*Hazard, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Hazard, error) {
	var h Hazard
	if err := s.db.WithContext(ctx).First(&h, id).Error; err != nil {
		return nil, fmt.Errorf("hazard not found: %w", err)
	}
	return &h, nil
}

func (s *Store) List(ctx context.Context) ([]Hazard, error) {
	var hazards []Hazard
	if err := s.db.WithContext(ctx).Find(&hazards).Error; err != nil {
		return nil, err
	}
	return hazards, nil
}

func (s *Store) Create(ctx context.Context, req CreateHazardRequest) (*Hazard, error) {
	h := Hazard{Name: req.Name, Description: req.Description}
	if err := s.db.WithContext(ctx).Create(&h).Error; err != nil {
		return nil, fmt.Errorf("create hazard failed: %w", err)
	}
	return &h, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&Hazard{}, id).Error
}
