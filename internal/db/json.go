package db

import (
	"context"
	"crawshaw.dev/jsonfile"
	"fmt"
)

type ArtistEntity struct {
	ID int64
}

type JsonRepository struct {
	db *jsonfile.JSONFile[ArtistEntity]
}

func NewJsonRepository(path string) (*JsonRepository, error) {
	db, err := jsonfile.New[ArtistEntity](path)
	if err != nil {
		return nil, fmt.Errorf("json repo: failed creating repository: %w", err)
	}
	return &JsonRepository{db}, err
}

func (r *JsonRepository) Add(ctx context.Context, artist ArtistEntity) error {
	err := r.db.Write(func(db *ArtistEntity) error {
		db.ID = artist.ID
		return nil
	})
	if err != nil {
		return fmt.Errorf("json repo: failed writing to json file: %w", err)
	}

	return nil
}

func (r *JsonRepository) ListAllFor(ctx context.Context, id string) ([]ArtistEntity, error) {
	var a ArtistEntity
	r.db.Read(func(db *ArtistEntity) {
		a = ArtistEntity{ID: db.ID}
	})
	return []ArtistEntity{a}, nil
}
