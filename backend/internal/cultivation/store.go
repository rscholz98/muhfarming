package cultivation

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Cultivation{}); err != nil {
		return nil, fmt.Errorf("migrate cultivation: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Cultivation, error)
	List(ctx context.Context) ([]Cultivation, error)
	Create(ctx context.Context, req CreateCultivationRequest) (*Cultivation, error)
	Update(ctx context.Context, id int64, req UpdateCultivationRequest) (*Cultivation, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Cultivation, error) {
	var c Cultivation
	if err := s.db.WithContext(ctx).First(&c, id).Error; err != nil {
		return nil, fmt.Errorf("cultivation not found: %w", err)
	}
	return &c, nil
}

func (s *Store) List(ctx context.Context) ([]Cultivation, error) {
	var cultivations []Cultivation
	if err := s.db.WithContext(ctx).Find(&cultivations).Error; err != nil {
		return nil, err
	}
	return cultivations, nil
}

func (s *Store) Create(ctx context.Context, req CreateCultivationRequest) (*Cultivation, error) {
	c := Cultivation{Name: req.Name, EstTimeToHarvestWeeks: req.EstTimeToHarvestWeeks}
	if err := s.db.WithContext(ctx).Create(&c).Error; err != nil {
		return nil, fmt.Errorf("create cultivation failed: %w", err)
	}
	return &c, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateCultivationRequest) (*Cultivation, error) {
	var c Cultivation
	if err := s.db.WithContext(ctx).First(&c, id).Error; err != nil {
		return nil, fmt.Errorf("cultivation not found: %w", err)
	}
	c.Name = req.Name
	c.EstTimeToHarvestWeeks = req.EstTimeToHarvestWeeks
	if err := s.db.WithContext(ctx).Save(&c).Error; err != nil {
		return nil, fmt.Errorf("update cultivation failed: %w", err)
	}
	return &c, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&Cultivation{}, id).Error
}
