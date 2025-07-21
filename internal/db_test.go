package internal_test

import (
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/internal"
	"os"
	"path/filepath"
	"testing"
)

func mustNewRepo(t *testing.T) *internal.JsonDb {
	return mustNewRepoWith(t, "")
}

func mustNewRepoWith(t *testing.T, fileContent string) *internal.JsonDb {
	dir := t.TempDir()
	path := filepath.Join(dir, "artist.json")

	if fileContent != "" {
		err := os.WriteFile(path, []byte(fileContent), 0600)
		if err != nil {
			t.Fatal(err)
		}
	}

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

	t.Run("picks up changes in existing file", func(t *testing.T) {
		existing := `
			{
			  "users": [
			    {
			      "telegramId": 1,
			      "artists": [
			        {
			          "name": "Leo Schick",
			          "soundcloudUrl": "https://soundcloud.com/randaleo",
			          "soundcloudUrn": "urn-1"
			        }
			      ]
			    }
			  ]
			}`

		repo := mustNewRepoWith(t, existing)
		user, found := repo.Get(1)
		assert.True(t, found)
		assert.Equal(t, "Leo Schick", user.Artists[0].Name)
	})

}
