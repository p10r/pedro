package internal_test

import (
	"github.com/p10r/pedro/internal"
	"github.com/p10r/pedro/internal/claude"
	"github.com/p10r/pedro/internal/soundcloud"
	"path/filepath"
	"testing"
)

// Spins up a testing in-memory environment which mocks off the SoundCloud API.
func mustNewInMemoryTestEnv(t *testing.T) *internal.Pedro {
	sc := soundcloud.NewFakeClient(t)

	dir := t.TempDir()
	path := filepath.Join(dir, "Artists.json")

	db, err := internal.NewJsonDb(path)
	if err != nil {
		t.Fatal("err when creating repository: %w", err)
	}

	ai := claude.NewFake()

	return internal.NewPedro(db, sc, ai)
}

// Spins up a testing environment which uses the real SoundCloud API.
func mustNewIntegrationTestEnv(t *testing.T, clientId string, clientSecret, claudeApiKey string) *internal.Pedro {
	t.Helper()

	sc := soundcloud.MustNewClient(t, soundcloud.TokenUrl, soundcloud.ApiUrl, clientId, clientSecret)

	dir := t.TempDir()
	path := filepath.Join(dir, "Artists.json")
	db, err := internal.NewJsonDb(path)
	if err != nil {
		t.Fatal("err when creating repository: %w", err)
	}

	ai := claude.NewClient(claude.ApiUrl, claudeApiKey)

	return internal.NewPedro(db, sc, ai)
}
