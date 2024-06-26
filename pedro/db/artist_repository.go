package db

import (
	"context"
	"github.com/p10r/pedro/pedro/domain"
	"github.com/p10r/pedro/pkg/sqlite"
)

type SqliteArtistRepository struct {
	db *sqlite.DB
}

func NewSqliteArtistRepository(db *sqlite.DB) *SqliteArtistRepository {
	return &SqliteArtistRepository{db: db}
}

func (r SqliteArtistRepository) Save(
	ctx context.Context,
	artist domain.Artist,
) (domain.Artist, error) {
	tx, err := r.db.BeginTx(ctx, nil)
	if err != nil {
		return domain.Artist{}, err
	}
	//nolint:errcheck
	defer tx.Rollback()

	e := newArtistDBEntity(artist)
	if e.ID == 0 {
		e, err = insertNewArtist(ctx, tx, e)
		if err != nil {
			return domain.Artist{}, err
		}
	} else {
		e, err = updateArtist(ctx, tx, e)
		if err != nil {
			return domain.Artist{}, err
		}
	}

	_ = tx.Commit()
	artist.ID = e.ID
	return artist, nil
}

func insertNewArtist(
	ctx context.Context,
	tx *sqlite.Tx,
	e artistDBEntity,
) (artistDBEntity, error) {
	result, err := tx.ExecContext(ctx, `
		INSERT INTO artists (
			ra_id,
			ra_slug,
			name,
			followers,
			tracked_events
		) VALUES (?,?,?,?,?)`,
		e.RAID, e.RASlug, e.Name, e.FollowedBy, e.TrackedEvents)
	if err != nil {
		return artistDBEntity{}, err
	}

	// Read back new e ID into caller argument.
	id, err := result.LastInsertId()
	if err != nil {
		return artistDBEntity{}, err
	}

	e.ID = id

	return e, nil
}

func updateArtist(
	ctx context.Context,
	tx *sqlite.Tx,
	e artistDBEntity,
) (artistDBEntity, error) {
	result, err := tx.ExecContext(ctx, `
		UPDATE artists
		SET ra_id =?,ra_slug = ?,name = ?,followers = ?,tracked_events = ?
		WHERE ID = ?`,
		e.RAID, e.RASlug, e.Name, e.FollowedBy, e.TrackedEvents, e.ID)
	if err != nil {
		return artistDBEntity{}, err
	}

	// Read back new e ID into caller argument.
	id, err := result.LastInsertId()
	if err != nil {
		return artistDBEntity{}, err
	}

	e.ID = id
	return e, nil
}

func (r SqliteArtistRepository) All(ctx context.Context) (domain.Artists, error) {
	tx, err := r.db.BeginTx(ctx, nil)
	if err != nil {
		return domain.Artists{}, err
	}
	//nolint:errcheck
	defer tx.Rollback()

	entities, err := findArtists(ctx, tx)
	if err != nil {
		return domain.Artists{}, err
	}

	var artists []domain.Artist
	for _, e := range entities {
		a := domain.Artist{
			ID:            e.ID,
			RAID:          e.RAID,
			RASlug:        domain.RASlug(e.RASlug),
			Name:          e.Name,
			FollowedBy:    e.FollowedBy.toUserIds(),
			TrackedEvents: e.TrackedEvents.toEventIds(),
		}
		artists = append(artists, a)
	}

	return artists, err
}

func findArtists(ctx context.Context, tx *sqlite.Tx) ([]*artistDBEntity, error) {
	rows, err := tx.QueryContext(ctx, `
		SELECT 
			id,
			ra_id,
			ra_slug,
			name,
			followers,
			tracked_events
		FROM artists 
		ORDER BY id`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	entities := make([]*artistDBEntity, 0)
	for rows.Next() {
		var e artistDBEntity
		err := rows.Scan(
			&e.ID,
			&e.RAID,
			&e.RASlug,
			&e.Name,
			&e.FollowedBy,
			&e.TrackedEvents,
		)
		if err != nil {
			return nil, err
		}
		entities = append(entities, &e)
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}

	return entities, nil
}
