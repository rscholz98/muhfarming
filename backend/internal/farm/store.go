package farm

import (
	"context"
	"errors"
	"fmt"

	"muhfarming/internal/auth"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&Farm{}); err != nil {
		return nil, fmt.Errorf("migrate farm: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Farm, error)
	List(ctx context.Context) ([]Farm, error)
	Create(ctx context.Context, req CreateFarmRequest) (*Farm, error)
	Update(ctx context.Context, id int64, req UpdateFarmRequest) (*Farm, error)
	Delete(ctx context.Context, id int64) error
}

// scoped applies farmer ownership filtering. Admins see all farms; farmers see
// only farms they own.
func scoped(ctx context.Context, db *gorm.DB) *gorm.DB {
	id, ok := auth.FromContext(ctx)
	if ok && id.IsAdmin() {
		return db
	}
	return db.Where("user_id = ?", id.UserID)
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Farm, error) {
	var f Farm
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("farm not found: %w", err)
	}
	return &f, nil
}

func (s *Store) List(ctx context.Context) ([]Farm, error) {
	var farms []Farm
	if err := scoped(ctx, s.db.WithContext(ctx)).Find(&farms).Error; err != nil {
		return nil, err
	}
	return farms, nil
}

func (s *Store) Create(ctx context.Context, req CreateFarmRequest) (*Farm, error) {
	id, ok := auth.FromContext(ctx)
	if !ok {
		return nil, errors.New("no authenticated user")
	}
	f := Farm{Name: req.Name, UserID: id.UserID}
	if err := s.db.WithContext(ctx).Create(&f).Error; err != nil {
		return nil, fmt.Errorf("create farm failed: %w", err)
	}
	return &f, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateFarmRequest) (*Farm, error) {
	var f Farm
	// Scope the lookup so a farmer cannot update another user's farm.
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("farm not found: %w", err)
	}
	f.Name = req.Name
	if err := s.db.WithContext(ctx).Save(&f).Error; err != nil {
		return nil, fmt.Errorf("update farm failed: %w", err)
	}
	return &f, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	// Scope the delete so a farmer cannot delete another user's farm.
	res := scoped(ctx, s.db.WithContext(ctx)).Delete(&Farm{}, id)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}
