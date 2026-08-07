package incident

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Incident{}); err != nil {
		return nil, fmt.Errorf("migrate incident: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Incident, error)
	List(ctx context.Context) ([]Incident, error)
	Create(ctx context.Context, req CreateIncidentRequest) (*Incident, error)
	Update(ctx context.Context, id int64, req UpdateIncidentRequest) (*Incident, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Incident, error) {
	var i Incident
	if err := s.db.WithContext(ctx).Preload("CultivationRisk").Preload("Region").First(&i, id).Error; err != nil {
		return nil, fmt.Errorf("incident not found: %w", err)
	}
	return &i, nil
}

func (s *Store) List(ctx context.Context) ([]Incident, error) {
	var incidents []Incident
	if err := s.db.WithContext(ctx).Preload("CultivationRisk").Preload("Region").Find(&incidents).Error; err != nil {
		return nil, err
	}
	return incidents, nil
}

func (s *Store) Create(ctx context.Context, req CreateIncidentRequest) (*Incident, error) {
	i := Incident{
		Date:              req.Date,
		Priority:          req.Priority,
		Description:       req.Description,
		CultivationRiskID: req.CultivationRiskID,
		RegionID:          req.RegionID,
	}
	if err := s.db.WithContext(ctx).Create(&i).Error; err != nil {
		return nil, fmt.Errorf("create incident failed: %w", err)
	}
	return s.GetByID(ctx, int64(i.ID))
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateIncidentRequest) (*Incident, error) {
	var i Incident
	if err := s.db.WithContext(ctx).First(&i, id).Error; err != nil {
		return nil, fmt.Errorf("incident not found: %w", err)
	}
	i.Date = req.Date
	i.Priority = req.Priority
	i.Description = req.Description
	i.CultivationRiskID = req.CultivationRiskID
	i.RegionID = req.RegionID
	if err := s.db.WithContext(ctx).Save(&i).Error; err != nil {
		return nil, fmt.Errorf("update incident failed: %w", err)
	}
	return s.GetByID(ctx, int64(i.ID))
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&Incident{}, id).Error
}
