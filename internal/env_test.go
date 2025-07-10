package internal_test

import (
	"github.com/p10r/pedro/internal"
	"github.com/p10r/pedro/internal/soundcloud"
	"path/filepath"
	"testing"
)

// Spins up a testing in-memory environment which mocks off the SoundCloud API.
func mustNewInMemoryTestEnv(t *testing.T) *internal.Service {
	sc := soundcloud.NewInMemoryClient(t)

	dir := t.TempDir()
	path := filepath.Join(dir, "Artists.json")

	repo, err := internal.NewJsonRepository(path)
	if err != nil {
		t.Fatal("err when creating repository: %w", err)
	}

	return internal.NewService(repo, sc)
}

// Spins up a testing environment which uses the real SoundCloud API.
func mustNewIntegrationTestEnv(t *testing.T, clientId string, clientSecret string) *internal.Service {
	t.Helper()

	soundcloudClient := soundcloud.MustNewClient(t, soundcloud.TokenUrl, soundcloud.ApiUrl, clientId, clientSecret)

	dir := t.TempDir()
	path := filepath.Join(dir, "Artists.json")
	repo, err := internal.NewJsonRepository(path)
	if err != nil {
		t.Fatal("err when creating repository: %w", err)
	}

	return internal.NewService(repo, soundcloudClient)
}
