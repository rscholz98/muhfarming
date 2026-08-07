package fieldcoordinate

import (
	"context"
	"fmt"

	"muhfarming/internal/auth"
	"muhfarming/internal/scope"

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

// scoped applies farmer ownership filtering. Admins see all coordinates;
// farmers see only coordinates on fields they own.
func scoped(ctx context.Context, db *gorm.DB) *gorm.DB {
	id, ok := auth.FromContext(ctx)
	if ok && id.IsAdmin() {
		return db
	}
	return db.Where("field_id IN (?)", scope.FieldIDs(db, id.UserID))
}

func (s *Store) GetByID(ctx context.Context, id int64) (*FieldCoordinate, error) {
	var fc FieldCoordinate
	if err := scoped(ctx, s.db.WithContext(ctx)).Preload("Field").First(&fc, id).Error; err != nil {
		return nil, fmt.Errorf("fieldcoordinate not found: %w", err)
	}
	return &fc, nil
}

func (s *Store) List(ctx context.Context) ([]FieldCoordinate, error) {
	var coords []FieldCoordinate
	if err := scoped(ctx, s.db.WithContext(ctx)).Preload("Field").Find(&coords).Error; err != nil {
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
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&fc, id).Error; err != nil {
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
	res := scoped(ctx, s.db.WithContext(ctx)).Delete(&FieldCoordinate{}, id)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}
