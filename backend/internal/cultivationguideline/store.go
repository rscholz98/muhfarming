package cultivationguideline

import (
	"context"
	"fmt"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&CultivationGuideline{}); err != nil {
		return nil, fmt.Errorf("migrate cultivationguideline: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*CultivationGuideline, error)
	List(ctx context.Context) ([]CultivationGuideline, error)
	Create(ctx context.Context, req CreateCultivationGuidelineRequest) (*CultivationGuideline, error)
	Update(ctx context.Context, id int64, req UpdateCultivationGuidelineRequest) (*CultivationGuideline, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*CultivationGuideline, error) {
	var g CultivationGuideline
	if err := s.db.WithContext(ctx).Preload("Cultivation").Preload("Fertilizer").First(&g, id).Error; err != nil {
		return nil, fmt.Errorf("cultivationguideline not found: %w", err)
	}
	return &g, nil
}

func (s *Store) List(ctx context.Context) ([]CultivationGuideline, error) {
	var guidelines []CultivationGuideline
	if err := s.db.WithContext(ctx).Preload("Cultivation").Preload("Fertilizer").Find(&guidelines).Error; err != nil {
		return nil, err
	}
	return guidelines, nil
}

func (s *Store) Create(ctx context.Context, req CreateCultivationGuidelineRequest) (*CultivationGuideline, error) {
	g := CultivationGuideline{
		Type:          req.Type,
		WeekFrom:      req.WeekFrom,
		WeekTo:        req.WeekTo,
		Instructions:  req.Instructions,
		CultivationID: req.CultivationID,
		FertilizerID:  req.FertilizerID,
	}
	if err := s.db.WithContext(ctx).Create(&g).Error; err != nil {
		return nil, fmt.Errorf("create cultivationguideline failed: %w", err)
	}
	return s.GetByID(ctx, int64(g.ID))
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateCultivationGuidelineRequest) (*CultivationGuideline, error) {
	var g CultivationGuideline
	if err := s.db.WithContext(ctx).First(&g, id).Error; err != nil {
		return nil, fmt.Errorf("cultivationguideline not found: %w", err)
	}
	g.Type = req.Type
	g.WeekFrom = req.WeekFrom
	g.WeekTo = req.WeekTo
	g.Instructions = req.Instructions
	g.CultivationID = req.CultivationID
	g.FertilizerID = req.FertilizerID
	if err := s.db.WithContext(ctx).Save(&g).Error; err != nil {
		return nil, fmt.Errorf("update cultivationguideline failed: %w", err)
	}
	return s.GetByID(ctx, int64(g.ID))
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&CultivationGuideline{}, id).Error
}
