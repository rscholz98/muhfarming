package farm

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Farm{}); err != nil {
		return nil, fmt.Errorf("migrate farm: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Farm, error)
	List(ctx context.Context) ([]Farm, error)
	Create(ctx context.Context, req CreateFarmRequest) (*Farm, error)
	Update(ctx context.Context, id int64, req UpdateFarmRequest) (*Farm, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Farm, error) {
	var f Farm
	if err := s.db.WithContext(ctx).First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("farm not found: %w", err)
	}
	return &f, nil
}

func (s *Store) List(ctx context.Context) ([]Farm, error) {
	var farms []Farm
	if err := s.db.WithContext(ctx).Find(&farms).Error; err != nil {
		return nil, err
	}
	return farms, nil
}

func (s *Store) Create(ctx context.Context, req CreateFarmRequest) (*Farm, error) {
	f := Farm{Name: req.Name}
	if err := s.db.WithContext(ctx).Create(&f).Error; err != nil {
		return nil, fmt.Errorf("create farm failed: %w", err)
	}
	return &f, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateFarmRequest) (*Farm, error) {
	var f Farm
	if err := s.db.WithContext(ctx).First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("farm not found: %w", err)
	}
	f.Name = req.Name
	if err := s.db.WithContext(ctx).Save(&f).Error; err != nil {
		return nil, fmt.Errorf("update farm failed: %w", err)
	}
	return &f, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&Farm{}, id).Error
}
