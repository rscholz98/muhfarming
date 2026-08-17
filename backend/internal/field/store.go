package field

import (
	"context"
	"fmt"
	"time"

	"muhfarming/internal/auth"
	"muhfarming/internal/scope"

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

// scoped applies farmer ownership filtering. Admins see all fields; farmers see
// only fields on farms they own.
func scoped(ctx context.Context, db *gorm.DB) *gorm.DB {
	id, ok := auth.FromContext(ctx)
	if ok && id.IsAdmin() {
		return db
	}
	return db.Where("farm_id IN (?)", scope.FarmIDs(db, id.UserID))
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Field, error) {
	var f Field
	if err := scoped(ctx, s.db.WithContext(ctx)).Preload("Farm").Preload("Region").First(&f, id).Error; err != nil {
		return nil, fmt.Errorf("field not found: %w", err)
	}
	return &f, nil
}

func (s *Store) List(ctx context.Context) ([]Field, error) {
	var fields []Field
	if err := scoped(ctx, s.db.WithContext(ctx)).Preload("Farm").Preload("Region").Find(&fields).Error; err != nil {
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
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&f, id).Error; err != nil {
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
	// Soft-delete the field and its coordinates together so we never leave
	// orphaned FieldCoordinate rows behind. The field package can't import
	// fieldcoordinate (that package imports this one), so the coordinates are
	// deleted by table name using GORM's soft-delete convention (deleted_at).
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		res := scoped(ctx, tx).Delete(&Field{}, id)
		if res.Error != nil {
			return res.Error
		}
		if res.RowsAffected == 0 {
			return gorm.ErrRecordNotFound
		}
		if err := tx.Table("field_coordinates").
			Where("field_id = ? AND deleted_at IS NULL", id).
			Update("deleted_at", time.Now()).Error; err != nil {
			return fmt.Errorf("delete field coordinates failed: %w", err)
		}
		return nil
	})
}
