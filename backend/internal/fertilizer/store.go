package fertilizer

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Fertilizer{}); err != nil {
		return nil, fmt.Errorf("migrate fertilizer: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Fertilizer, error)
	List(ctx context.Context) ([]Fertilizer, error)
	Create(ctx context.Context, req CreateFertilizerRequest) (*Fertilizer, error)
	Update(ctx context.Context, id int64, req UpdateFertilizerRequest) (*Fertilizer, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Fertilizer, error) {
	var f Fertilizer
	if err := s.db.WithContext(ctx).First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("fertilizer not found: %w", err)
	}
	return &f, nil
}

func (s *Store) List(ctx context.Context) ([]Fertilizer, error) {
	var fertilizers []Fertilizer
	if err := s.db.WithContext(ctx).Find(&fertilizers).Error; err != nil {
		return nil, err
	}
	return fertilizers, nil
}

func (s *Store) Create(ctx context.Context, req CreateFertilizerRequest) (*Fertilizer, error) {
	f := Fertilizer{Name: req.Name}
	if err := s.db.WithContext(ctx).Create(&f).Error; err != nil {
		return nil, fmt.Errorf("create fertilizer failed: %w", err)
	}
	return &f, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateFertilizerRequest) (*Fertilizer, error) {
	var f Fertilizer
	if err := s.db.WithContext(ctx).First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("fertilizer not found: %w", err)
	}
	f.Name = req.Name
	if err := s.db.WithContext(ctx).Save(&f).Error; err != nil {
		return nil, fmt.Errorf("update fertilizer failed: %w", err)
	}
	return &f, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&Fertilizer{}, id).Error
}
