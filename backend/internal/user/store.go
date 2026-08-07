package user

import (
	"context"
	"fmt"

	"muhfarming/internal/auth"

	"gorm.io/gorm"
)

type Store struct {
	db *gorm.DB
}

func NewStore(db *gorm.DB) (*Store, error) {
	if err := db.AutoMigrate(&User{}); err != nil {
		return nil, fmt.Errorf("migrate user: %w", err)
	}
	return &Store{db: db}, nil
}

type store interface {
	GetByID(ctx context.Context, id int64) (*User, error)
	List(ctx context.Context) ([]User, error)
	Create(ctx context.Context, req CreateUserRequest) (*User, error)
	Update(ctx context.Context, id int64, req UpdateUserRequest) (*User, error)
	Delete(ctx context.Context, id int64) error
}

func (s *Store) GetByID(ctx context.Context, id int64) (*User, error) {
	var u User
	if err := s.db.WithContext(ctx).First(&u, id).Error; err != nil {
		return nil, fmt.Errorf("user not found: %w", err)
	}
	return &u, nil
}

func (s *Store) List(ctx context.Context) ([]User, error) {
	var users []User
	if err := s.db.WithContext(ctx).Find(&users).Error; err != nil {
		return nil, err
	}
	return users, nil
}

func (s *Store) Create(ctx context.Context, req CreateUserRequest) (*User, error) {
	hash, err := auth.HashPassword(req.Password)
	if err != nil {
		return nil, fmt.Errorf("hash password: %w", err)
	}
	u := User{
		Username:     req.Username,
		PasswordHash: hash,
		Name:         req.Name,
		Surname:      req.Surname,
		Title:        req.Title,
		Email:        req.Email,
		Language:     req.Language,
		Role:         req.Role,
	}
	if err := s.db.WithContext(ctx).Create(&u).Error; err != nil {
		return nil, fmt.Errorf("create user failed: %w", err)
	}
	return &u, nil
}

func (s *Store) Update(ctx context.Context, id int64, req UpdateUserRequest) (*User, error) {
	var u User
	if err := s.db.WithContext(ctx).First(&u, id).Error; err != nil {
		return nil, fmt.Errorf("user not found: %w", err)
	}
	u.Name = req.Name
	u.Surname = req.Surname
	u.Title = req.Title
	u.Email = req.Email
	u.Language = req.Language
	u.Role = req.Role
	if req.Password != "" {
		hash, err := auth.HashPassword(req.Password)
		if err != nil {
			return nil, fmt.Errorf("hash password: %w", err)
		}
		u.PasswordHash = hash
	}
	if err := s.db.WithContext(ctx).Save(&u).Error; err != nil {
		return nil, fmt.Errorf("update user failed: %w", err)
	}
	return &u, nil
}

func (s *Store) Delete(ctx context.Context, id int64) error {
	return s.db.WithContext(ctx).Delete(&User{}, id).Error
}

// GetByUsername looks up a user by their unique username. Used by the auth
// login flow.
func (s *Store) GetByUsername(ctx context.Context, username string) (*User, error) {
	var u User
	if err := s.db.WithContext(ctx).Where("username = ?", username).First(&u).Error; err != nil {
		return nil, fmt.Errorf("user not found: %w", err)
	}
	return &u, nil
}

// --- auth.UserStore adapter ---
// These methods let *Store satisfy the auth.UserStore interface used by the
// signup/login handlers, without the auth package depending on this one.

// CreateFarmer registers a new farmer with the given plaintext password.
func (s *Store) CreateFarmer(ctx context.Context, username, password string) (auth.UserRecord, error) {
	hash, err := auth.HashPassword(password)
	if err != nil {
		return auth.UserRecord{}, fmt.Errorf("hash password: %w", err)
	}
	u := User{
		Username:     username,
		PasswordHash: hash,
		Name:         username,
		Email:        "",
		Role:         auth.RoleFarmer,
	}
	if err := s.db.WithContext(ctx).Create(&u).Error; err != nil {
		return auth.UserRecord{}, fmt.Errorf("create farmer failed: %w", err)
	}
	return toRecord(u), nil
}

// FindByUsername returns the auth record for login verification.
func (s *Store) FindByUsername(ctx context.Context, username string) (auth.UserRecord, error) {
	u, err := s.GetByUsername(ctx, username)
	if err != nil {
		return auth.UserRecord{}, err
	}
	return toRecord(*u), nil
}

func toRecord(u User) auth.UserRecord {
	return auth.UserRecord{
		ID:           u.ID,
		Username:     u.Username,
		PasswordHash: u.PasswordHash,
		Role:         u.Role,
	}
}

// EnsureAdmin creates the admin user if it does not already exist. Idempotent;
// used to seed the admin from deployment environment variables.
func (s *Store) EnsureAdmin(ctx context.Context, username, password string) error {
	var count int64
	if err := s.db.WithContext(ctx).Model(&User{}).Where("username = ?", username).Count(&count).Error; err != nil {
		return err
	}
	if count > 0 {
		return nil
	}
	hash, err := auth.HashPassword(password)
	if err != nil {
		return fmt.Errorf("hash admin password: %w", err)
	}
	admin := User{
		Username:     username,
		PasswordHash: hash,
		Name:         "Administrator",
		Email:        "",
		Role:         auth.RoleAdmin,
	}
	if err := s.db.WithContext(ctx).Create(&admin).Error; err != nil {
		return fmt.Errorf("create admin failed: %w", err)
	}
	return nil
}
