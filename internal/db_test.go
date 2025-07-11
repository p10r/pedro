package internal_test

import (
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/internal"
	"path/filepath"
	"testing"
)

func mustNewRepo(t *testing.T) *internal.JsonDb {
	dir := t.TempDir()
	path := filepath.Join(dir, "artists.json")
	repo, err := internal.NewJsonDb(path)
	if err != nil {
		t.Fatal(err)
	}
	return repo
}

func TestJsonFile(t *testing.T) {
	t.Parallel()

	t.Run("write and read user to/from json file", func(t *testing.T) {
		repo := mustNewRepo(t)

		userId := int64(581274912)
		a := internal.UserEntity{TelegramId: userId}

		err := repo.Save(a)
		assert.NoError(t, err)

		user, found := repo.Get(userId)
		assert.True(t, found)
		assert.Equal(t, userId, user.TelegramId)

		assert.Equal(t, 1, len(repo.All().Users))
	})

	t.Run("updates existing user", func(t *testing.T) {
		repo := mustNewRepo(t)

		userId := int64(124152)
		entity := internal.UserEntity{TelegramId: userId}
		err := repo.Save(entity)
		assert.NoError(t, err)
		assert.Equal(t, 1, len(repo.All().Users))

		user, found := repo.Get(userId)
		assert.True(t, found)
		assert.Zero(t, user.Artists)

		entity.Artists = []internal.ArtistEntity{{
			Name: "10 Mark DJ Team",
		}}
		err = repo.Save(entity)
		assert.NoError(t, err)

		user, found = repo.Get(userId)
		assert.True(t, found)
		assert.NotZero(t, user.Artists)
		assert.Equal(t, "10 Mark DJ Team", user.Artists[0].Name)
	})
}
