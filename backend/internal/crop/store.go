package crop

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
	if err := db.AutoMigrate(&Crop{}); err != nil {
		return nil, fmt.Errorf("migrate crop: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Crop, error)
	List(ctx context.Context) ([]Crop, error)
	Create(ctx context.Context, req CreateCropRequest) (*Crop, error)
	Update(ctx context.Context, id int64, req UpdateCropRequest) (*Crop, error)
	Delete(ctx context.Context, id int64) error
}

// scoped applies farmer ownership filtering. Admins see all crops; farmers see
// only crops on fields they own.
func scoped(ctx context.Context, db *gorm.DB) *gorm.DB {
	id, ok := auth.FromContext(ctx)
	if ok && id.IsAdmin() {
		return db
	}
	return db.Where("field_id IN (?)", scope.FieldIDs(db, id.UserID))
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Crop, error) {
	var c Crop
	if err := scoped(ctx, s.db.WithContext(ctx)).Preload("Field").Preload("Cultivation").First(&c, id).Error; err != nil {
		return nil, fmt.Errorf("crop not found: %w", err)
	}
	return &c, nil
}

func (s *Store) List(ctx context.Context) ([]Crop, error) {
	var crops []Crop
	if err := scoped(ctx, s.db.WithContext(ctx)).Preload("Field").Preload("Cultivation").Find(&crops).Error; err != nil {
		return nil, err
	}
	return crops, nil
}

func (s *Store) Create(ctx context.Context, req CreateCropRequest) (*Crop, error) {
	c := Crop{
		Status:        req.Status,
		DatePlanted:   req.DatePlanted,
		LastUpdated:   req.LastUpdated,
		FieldID:       req.FieldID,
		CultivationID: req.CultivationID,
	}
	if err := s.db.WithContext(ctx).Create(&c).Error; err != nil {
		return nil, fmt.Errorf("create crop failed: %w", err)
	}
	return s.GetByID(ctx, int64(c.ID))
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateCropRequest) (*Crop, error) {
	var c Crop
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&c, id).Error; err != nil {
		return nil, fmt.Errorf("crop not found: %w", err)
	}
	c.Status = req.Status
	c.DatePlanted = req.DatePlanted
	c.LastUpdated = req.LastUpdated
	c.FieldID = req.FieldID
	c.CultivationID = req.CultivationID
	if err := s.db.WithContext(ctx).Save(&c).Error; err != nil {
		return nil, fmt.Errorf("update crop failed: %w", err)
	}
	return s.GetByID(ctx, int64(c.ID))
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	res := scoped(ctx, s.db.WithContext(ctx)).Delete(&Crop{}, id)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}
