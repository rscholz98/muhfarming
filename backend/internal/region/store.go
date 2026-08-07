package region

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Region{}); err != nil {
		return nil, fmt.Errorf("migrate region: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Region, error)
	List(ctx context.Context) ([]Region, error)
	Create(ctx context.Context, req CreateRegionRequest) (*Region, error)
	Update(ctx context.Context, id int64, req UpdateRegionRequest) (*Region, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Region, error) {
	var r Region
	if err := s.db.WithContext(ctx).First(&r, id).Error; err != nil {
		return nil, fmt.Errorf("region not found: %w", err)
	}
	return &r, nil
}

func (s *Store) List(ctx context.Context) ([]Region, error) {
	var regions []Region
	if err := s.db.WithContext(ctx).Find(&regions).Error; err != nil {
		return nil, err
	}
	return regions, nil
}

func (s *Store) Create(ctx context.Context, req CreateRegionRequest) (*Region, error) {
	r := Region{Name: req.Name, GeoCode: req.GeoCode}
	if err := s.db.WithContext(ctx).Create(&r).Error; err != nil {
		return nil, fmt.Errorf("create region failed: %w", err)
	}
	return &r, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateRegionRequest) (*Region, error) {
	var r Region
	if err := s.db.WithContext(ctx).First(&r, id).Error; err != nil {
		return nil, fmt.Errorf("region not found: %w", err)
	}
	r.Name = req.Name
	r.GeoCode = req.GeoCode
	if err := s.db.WithContext(ctx).Save(&r).Error; err != nil {
		return nil, fmt.Errorf("update region failed: %w", err)
	}
	return &r, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&Region{}, id).Error
}
