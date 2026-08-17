package region

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
	if err := db.AutoMigrate(&Region{}); err != nil {
		return nil, fmt.Errorf("migrate region: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*Region, error)
	List(ctx context.Context) ([]Region, error)
	Create(ctx context.Context, req CreateRegionRequest) (*Region, error)
	Update(ctx context.Context, id int64, req UpdateRegionRequest) (*Region, error)
	Delete(ctx context.Context, id int64) error
}

// scoped applies farmer ownership filtering. Admins see all regions; farmers
// see only regions they operate in.
func scoped(ctx context.Context, db *gorm.DB) *gorm.DB {
	id, ok := auth.FromContext(ctx)
	if ok && id.IsAdmin() {
		return db
	}
	return db.Where("id IN (?)", scope.RegionIDs(db, id.UserID))
}

func (s *Store) GetByID(ctx context.Context, id int64) (*Region, error) {
	var r Region
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&r, id).Error; err != nil {
		return nil, fmt.Errorf("region not found: %w", err)
	}
	return &r, nil
}

func (s *Store) List(ctx context.Context) ([]Region, error) {
	var regions []Region
	if err := scoped(ctx, s.db.WithContext(ctx)).Find(&regions).Error; err != nil {
		return nil, err
	}
	return regions, nil
}

func (s *Store) Create(ctx context.Context, req CreateRegionRequest) (*Region, error) {
	r := Region{Name: req.Name, GeoCode: req.GeoCode}
	if err := s.db.WithContext(ctx).Create(&r).Error; err != nil {
		return nil, fmt.Errorf("create region failed: %w", err)
	}
	return &r, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateRegionRequest) (*Region, error) {
	var r Region
	if err := scoped(ctx, s.db.WithContext(ctx)).First(&r, id).Error; err != nil {
		return nil, fmt.Errorf("region not found: %w", err)
	}
	r.Name = req.Name
	r.GeoCode = req.GeoCode
	if err := s.db.WithContext(ctx).Save(&r).Error; err != nil {
		return nil, fmt.Errorf("update region failed: %w", err)
	}
	return &r, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	res := scoped(ctx, s.db.WithContext(ctx)).Delete(&Region{}, id)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

// seedRegions is the canonical list of Cameroon's 10 regions, in the order they
// are seeded. On a fresh database this assigns Adamawa=1, Centre=2, ... West=10.
var seedRegions = []Region{
	{Name: "Adamawa", GeoCode: "AD"},
	{Name: "Centre", GeoCode: "CE"},
	{Name: "East", GeoCode: "ES"},
	{Name: "Extreme North", GeoCode: "EN"},
	{Name: "Littoral", GeoCode: "LT"},
	{Name: "North", GeoCode: "NO"},
	{Name: "Northwest", GeoCode: "NW"},
	{Name: "South", GeoCode: "SU"},
	{Name: "Southwest", GeoCode: "SW"},
	{Name: "West", GeoCode: "OU"},
}

// EnsureRegions seeds the 10 Cameroon regions if they do not already exist.
// Idempotent: each region is matched by name, so repeated calls (across
// restarts) never create duplicates.
func (s *Store) EnsureRegions(ctx context.Context) error {
	for _, r := range seedRegions {
		var existing Region
		if err := s.db.WithContext(ctx).
			Where(Region{Name: r.Name}).
			Attrs(Region{GeoCode: r.GeoCode}).
			FirstOrCreate(&existing).Error; err != nil {
			return fmt.Errorf("seed region %q: %w", r.Name, err)
		}
	}
	return nil
}
