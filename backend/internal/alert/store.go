package alert

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Alert{}); err != nil {
		return nil, fmt.Errorf("migrate alert: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Alert, error)
	List(ctx context.Context) ([]Alert, error)
	Create(ctx context.Context, req CreateAlertRequest) (*Alert, error)
	Update(ctx context.Context, id int64, req UpdateAlertRequest) (*Alert, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Alert, error) {
	var a Alert
	if err := s.db.WithContext(ctx).Preload("Field").Preload("Incident").First(&a, id).Error; err != nil {
		return nil, fmt.Errorf("alert not found: %w", err)
	}
	return &a, nil
}

func (s *Store) List(ctx context.Context) ([]Alert, error) {
	var alerts []Alert
	if err := s.db.WithContext(ctx).Preload("Field").Preload("Incident").Find(&alerts).Error; err != nil {
		return nil, err
	}
	return alerts, nil
}

func (s *Store) Create(ctx context.Context, req CreateAlertRequest) (*Alert, error) {
	a := Alert{
		FieldID:    req.FieldID,
		IncidentID: req.IncidentID,
	}
	if err := s.db.WithContext(ctx).Create(&a).Error; err != nil {
		return nil, fmt.Errorf("create alert failed: %w", err)
	}
	return &a, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateAlertRequest) (*Alert, error) {
	var a Alert
	if err := s.db.WithContext(ctx).First(&a, id).Error; err != nil {
		return nil, fmt.Errorf("alert not found: %w", err)
	}
	a.FieldID = req.FieldID
	a.IncidentID = req.IncidentID
	if err := s.db.WithContext(ctx).Save(&a).Error; err != nil {
		return nil, fmt.Errorf("update alert failed: %w", err)
	}
	return &a, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&Alert{}, id).Error
}
