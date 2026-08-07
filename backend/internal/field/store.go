package field

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Field{}); err != nil {
		return nil, fmt.Errorf("migrate field: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Field, error)
	List(ctx context.Context) ([]Field, error)
	Create(ctx context.Context, req CreateFieldRequest) (*Field, error)
	Update(ctx context.Context, id int64, req UpdateFieldRequest) (*Field, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Field, error) {
	var f Field
	if err := s.db.WithContext(ctx).Preload("Farm").Preload("Region").First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("field not found: %w", err)
	}
	return &f, nil
}

func (s *Store) List(ctx context.Context) ([]Field, error) {
	var fields []Field
	if err := s.db.WithContext(ctx).Preload("Farm").Preload("Region").Find(&fields).Error; err != nil {
		return nil, err
	}
	return fields, nil
}

func (s *Store) Create(ctx context.Context, req CreateFieldRequest) (*Field, error) {
	f := Field{
		Name:       req.Name,
		FieldNotes: req.FieldNotes,
		FarmID:     req.FarmID,
		RegionID:   req.RegionID,
	}
	if err := s.db.WithContext(ctx).Create(&f).Error; err != nil {
		return nil, fmt.Errorf("create field failed: %w", err)
	}
	return s.GetByID(ctx, int64(f.ID))
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateFieldRequest) (*Field, error) {
	var f Field
	if err := s.db.WithContext(ctx).First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("field not found: %w", err)
	}
	f.Name = req.Name
	f.FieldNotes = req.FieldNotes
	f.FarmID = req.FarmID
	f.RegionID = req.RegionID
	if err := s.db.WithContext(ctx).Save(&f).Error; err != nil {
		return nil, fmt.Errorf("update field failed: %w", err)
	}
	return s.GetByID(ctx, int64(f.ID))
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&Field{}, id).Error
}
