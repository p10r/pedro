package db_test

import (
	"context"
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/internal"
	"github.com/p10r/pedro/internal/db"
	"os"
	"path/filepath"
	"testing"
)

func TestJsonFile(t *testing.T) {
	ctx := context.Background()
	dumpToFile := os.Getenv("DB_JSON_DUMP_TO_FILE")
	dir := t.TempDir()
	if dumpToFile == "1" {
		dir = "/local"
	}

	t.Run("write and read artist to/from json file", func(t *testing.T) {
		path := filepath.Join(dir, "artists.json")

		repo, err := db.NewJsonRepository(path)
		assert.NoError(t, err)

		artistId := int64(581274912)
		a := internal.Artist{ID: artistId}

		err = repo.Add(ctx, a)
		assert.NoError(t, err)

		artists, err := repo.ListAllFor(ctx, "1")
		assert.NoError(t, err)

		assert.Equal(t, artistId, artists[0].ID)
	})

}
