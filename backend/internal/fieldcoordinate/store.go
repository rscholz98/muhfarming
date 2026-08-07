package fieldcoordinate

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&FieldCoordinate{}); err != nil {
		return nil, fmt.Errorf("migrate fieldcoordinate: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*FieldCoordinate, error)
	List(ctx context.Context) ([]FieldCoordinate, error)
	Create(ctx context.Context, req CreateFieldCoordinateRequest) (*FieldCoordinate, error)
	Update(ctx context.Context, id int64, req UpdateFieldCoordinateRequest) (*FieldCoordinate, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*FieldCoordinate, error) {
	var fc FieldCoordinate
	if err := s.db.WithContext(ctx).Preload("Field").First(&fc, id).Error; err != nil {
		return nil, fmt.Errorf("fieldcoordinate not found: %w", err)
	}
	return &fc, nil
}

func (s *Store) List(ctx context.Context) ([]FieldCoordinate, error) {
	var coords []FieldCoordinate
	if err := s.db.WithContext(ctx).Preload("Field").Find(&coords).Error; err != nil {
		return nil, err
	}
	return coords, nil
}

func (s *Store) Create(ctx context.Context, req CreateFieldCoordinateRequest) (*FieldCoordinate, error) {
	fc := FieldCoordinate{
		Latitude:      req.Latitude,
		Longitude:     req.Longitude,
		SequenceOrder: req.SequenceOrder,
		FieldID:       req.FieldID,
	}
	if err := s.db.WithContext(ctx).Create(&fc).Error; err != nil {
		return nil, fmt.Errorf("create fieldcoordinate failed: %w", err)
	}
	return s.GetByID(ctx, int64(fc.ID))
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateFieldCoordinateRequest) (*FieldCoordinate, error) {
	var fc FieldCoordinate
	if err := s.db.WithContext(ctx).First(&fc, id).Error; err != nil {
		return nil, fmt.Errorf("fieldcoordinate not found: %w", err)
	}
	fc.Latitude = req.Latitude
	fc.Longitude = req.Longitude
	fc.SequenceOrder = req.SequenceOrder
	fc.FieldID = req.FieldID
	if err := s.db.WithContext(ctx).Save(&fc).Error; err != nil {
		return nil, fmt.Errorf("update fieldcoordinate failed: %w", err)
	}
	return s.GetByID(ctx, int64(fc.ID))
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&FieldCoordinate{}, id).Error
}
