package internal_test

import (
	"context"
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/internal"
	"github.com/p10r/pedro/internal/db"
	"github.com/p10r/pedro/internal/soundcloud"
	"os"
	"path/filepath"
	"testing"
)

func mustNewRepository(t *testing.T) *db.JsonRepository {
	t.Helper()

	dir := t.TempDir()
	path := filepath.Join(dir, "Artists.json")

	repo, err := db.NewJsonRepository(path)
	if err != nil {
		t.Fatal("err when creating repository: %w", err)
	}

	return repo
}

func mustNewService(t *testing.T, tokenUrl, apiUrl, clientId, clientSecret string) *internal.Service {
	t.Helper()

	sc := soundcloud.MustNewClient(t, tokenUrl, apiUrl, clientId, clientSecret)
	repo := mustNewRepository(t)
	return internal.NewService(repo, sc)
}

func TestFollowingArtists(t *testing.T) {
	t.Parallel()

	clientId := os.Getenv("SOUNDCLOUD_CLIENT_ID")
	clientSecret := os.Getenv("SOUNDCLOUD_CLIENT_SECRET")
	if clientSecret == "" || clientId == "" {
		t.Skip("set SOUNDCLOUD_CLIENT_SECRET and SOUNDCLOUD_CLIENT_ID to run this test")
	}

	ctx := context.Background()
	service := mustNewService(t, soundcloud.TokenUrl, soundcloud.ApiUrl, clientId, clientSecret)

	t.Run("follow an artist", func(t *testing.T) {
		userId := internal.UserId(44124)
		cmd := internal.FollowArtistCmd{SoundcloudUrl: "https://soundcloud.com/hovrmusic", UserId: userId}

		_, err := service.FollowArtist(ctx, cmd)
		assert.NoError(t, err)

		res, err := service.ListArtists(ctx, userId)
		assert.NoError(t, err)

		expected := internal.Artists{{
			Name: "HOVR",
			Url:  "https://soundcloud.com/hovrmusic",
		}}
		assert.Equal(t, expected, res)
	})

	t.Run("try following an artist that doesn't exist", func(t *testing.T) {
		t.Skip("TODO: Implement me")
	})

	t.Run("try following the same artist twice", func(t *testing.T) {
		t.Skip("TODO: Implement me")
	})
}
