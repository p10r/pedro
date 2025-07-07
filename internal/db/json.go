package db

import (
	"context"
	"crawshaw.dev/jsonfile"
	"fmt"
	"github.com/p10r/pedro/internal"
)

type JsonRepository struct {
	db *jsonfile.JSONFile[internal.Artist]
}

func NewJsonRepository(path string) (*JsonRepository, error) {
	db, err := jsonfile.New[internal.Artist](path)
	if err != nil {
		return nil, fmt.Errorf("json repo: failed creating repository: %w", err)
	}
	return &JsonRepository{db}, err
}

func (r *JsonRepository) Add(ctx context.Context, artist internal.Artist) error {
	err := r.db.Write(func(db *internal.Artist) error {
		db.ID = artist.ID
		return nil
	})
	if err != nil {
		return fmt.Errorf("json repo: failed writing to json file: %w", err)
	}

	return nil
}

func (r *JsonRepository) ListAllFor(ctx context.Context, id internal.UserId) ([]internal.Artist, error) {
	var a internal.Artist
	r.db.Read(func(db *internal.Artist) {
		a = internal.Artist{ID: db.ID}
	})
	return []internal.Artist{a}, nil
}
