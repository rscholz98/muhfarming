package cultivation

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

// scoped applies farmer ownership filtering. Admins see all cultivations;
// farmers see only cultivations they grow.
func scoped(ctx context.Context, db *gorm.DB) *gorm.DB {
	id, ok := auth.FromContext(ctx)
	if ok && id.IsAdmin() {
		return db
	}
	return db.Where("id IN (?)", scope.CultivationIDs(db, id.UserID))
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Cultivation, error) {
	var c Cultivation
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&c, id).Error; err != nil {
		return nil, fmt.Errorf("cultivation not found: %w", err)
	}
	return &c, nil
}

func (s *Store) List(ctx context.Context) ([]Cultivation, error) {
	var cultivations []Cultivation
	if err := scoped(ctx, s.db.WithContext(ctx)).Find(&cultivations).Error; err != nil {
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
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&c, id).Error; err != nil {
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
	res := scoped(ctx, s.db.WithContext(ctx)).Delete(&Cultivation{}, id)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}
