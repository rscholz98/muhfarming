// Package scope builds GORM query scopes that restrict entity queries to the
// data a given user is allowed to see. Admins bypass all scoping.
//
// A farmer's visible data is derived from ownership: a user owns many farms,
// and from those farms flow the visible fields, regions, and cultivations.
//
//	myFarmIDs        = farms.id          where farms.user_id = me
//	myFieldIDs       = fields.id         where fields.farm_id in myFarmIDs
//	myRegionIDs      = fields.region_id  where fields.farm_id in myFarmIDs
//	myCultivationIDs = crops.cultivation_id where crops.field_id in myFieldIDs
//
// Each helper returns a *gorm.DB carrying an unexecuted subquery suitable for
// use with `.Where("col IN (?)", subquery)`.
package scope

import "gorm.io/gorm"

// FarmIDs is a subquery selecting the ids of farms owned by the user.
func FarmIDs(db *gorm.DB, userID uint) *gorm.DB {
	return db.Model(nil).Table("farms").
		Select("id").
		Where("user_id = ? AND deleted_at IS NULL", userID)
}

// FieldIDs is a subquery selecting the ids of fields on the user's farms.
func FieldIDs(db *gorm.DB, userID uint) *gorm.DB {
	return db.Table("fields").
		Select("id").
		Where("farm_id IN (?) AND deleted_at IS NULL", FarmIDs(db, userID))
}

// RegionIDs is a subquery selecting the distinct regions the user operates in.
func RegionIDs(db *gorm.DB, userID uint) *gorm.DB {
	return db.Table("fields").
		Select("DISTINCT region_id").
		Where("farm_id IN (?) AND deleted_at IS NULL", FarmIDs(db, userID))
}

// CultivationIDs is a subquery selecting the distinct cultivations the user
// grows (via crops planted on their fields).
func CultivationIDs(db *gorm.DB, userID uint) *gorm.DB {
	return db.Table("crops").
		Select("DISTINCT cultivation_id").
		Where("field_id IN (?) AND deleted_at IS NULL", FieldIDs(db, userID))
}
