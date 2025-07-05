package soundcloud

import (
	"github.com/alecthomas/assert/v2"
	"os"
	"testing"
)

func TestProdSoundcloud(t *testing.T) {
	clientId := os.Getenv("SOUNDCLOUD_CLIENT_ID")
	clientSecret := os.Getenv("SOUNDCLOUD_CLIENT_SECRET")
	// This is provided via env, since it doesn't seem to be publicly exposed by SoundCloud, so this adheres to it.
	exampleArtistUrn := os.Getenv("SOUNDCLOUD_ARTIST_URN")
	if clientSecret == "" || clientId == "" || exampleArtistUrn == "" {
		t.Skip("set SOUNDCLOUD_CLIENT_SECRET, SOUNDCLOUD_CLIENT_ID and SOUNDCLOUD_ARTIST_URN to run this test")
	}

	const (
		tokenUrl = "https://secure.soundcloud.com/oauth/token"
		apiUrl   = "https://api.soundcloud.com/"
	)

	t.Run("fetches artist by url", func(t *testing.T) {
		c := mustNewClient(t, tokenUrl, apiUrl, clientId, clientSecret)

		res, err := c.ArtistByUrl("https://soundcloud.com/bizzarro_universe")
		assert.NoError(t, err)
		assert.Equal(t, "Bizzarro Universe", res.Username)
	})

	t.Run("fetches artist by urn", func(t *testing.T) {
		c := mustNewClient(t, tokenUrl, apiUrl, clientId, clientSecret)

		res, err := c.ArtistByUrn(exampleArtistUrn)
		assert.NoError(t, err)
		assert.Equal(t, "Bizzarro Universe", res.Username)
	})

}

func mustNewClient(t *testing.T, tokenUrl, apiUrl, clientId, clientSecret string) *client {
	conf, err := newOAuthConfig(clientId, clientSecret, tokenUrl)
	if err != nil {
		t.Fatal("could not create oauth conf %w", err)
	}

	c, err := newClient(conf, apiUrl)
	if err != nil {
		t.Fatal("could not create client %w", err)
	}
	return c
}
