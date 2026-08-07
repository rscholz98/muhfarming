package cultivationrisk

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
	if err := db.AutoMigrate(&CultivationRisk{}); err != nil {
		return nil, fmt.Errorf("migrate cultivationrisk: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*CultivationRisk, error)
	List(ctx context.Context) ([]CultivationRisk, error)
	Create(ctx context.Context, req CreateCultivationRiskRequest) (*CultivationRisk, error)
	Update(ctx context.Context, id int64, req UpdateCultivationRiskRequest) (*CultivationRisk, error)
	Delete(ctx context.Context, id int64) error
}

// scoped applies farmer ownership filtering. Admins see all risks; farmers see
// only risks for cultivations they grow.
func scoped(ctx context.Context, db *gorm.DB) *gorm.DB {
	id, ok := auth.FromContext(ctx)
	if ok && id.IsAdmin() {
		return db
	}
	return db.Where("cultivation_id IN (?)", scope.CultivationIDs(db, id.UserID))
}

func (s *Store) GetByID(ctx context.Context, id int64) (*CultivationRisk, error) {
	var cr CultivationRisk
	if err := scoped(ctx, s.db.WithContext(ctx)).Preload("Cultivation").Preload("Hazard").First(&cr, id).Error; err != nil {
		return nil, fmt.Errorf("cultivationrisk not found: %w", err)
	}
	return &cr, nil
}

func (s *Store) List(ctx context.Context) ([]CultivationRisk, error) {
	var risks []CultivationRisk
	if err := scoped(ctx, s.db.WithContext(ctx)).Preload("Cultivation").Preload("Hazard").Find(&risks).Error; err != nil {
		return nil, err
	}
	return risks, nil
}

func (s *Store) Create(ctx context.Context, req CreateCultivationRiskRequest) (*CultivationRisk, error) {
	cr := CultivationRisk{
		WeekFrom:      req.WeekFrom,
		WeekTo:        req.WeekTo,
		Solution:      req.Solution,
		CultivationID: req.CultivationID,
		HazardID:      req.HazardID,
	}
	if err := s.db.WithContext(ctx).Create(&cr).Error; err != nil {
		return nil, fmt.Errorf("create cultivationrisk failed: %w", err)
	}
	return s.GetByID(ctx, int64(cr.ID))
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateCultivationRiskRequest) (*CultivationRisk, error) {
	var cr CultivationRisk
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&cr, id).Error; err != nil {
		return nil, fmt.Errorf("cultivationrisk not found: %w", err)
	}
	cr.WeekFrom = req.WeekFrom
	cr.WeekTo = req.WeekTo
	cr.Solution = req.Solution
	cr.CultivationID = req.CultivationID
	cr.HazardID = req.HazardID
	if err := s.db.WithContext(ctx).Save(&cr).Error; err != nil {
		return nil, fmt.Errorf("update cultivationrisk failed: %w", err)
	}
	return s.GetByID(ctx, int64(cr.ID))
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	res := scoped(ctx, s.db.WithContext(ctx)).Delete(&CultivationRisk{}, id)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}
