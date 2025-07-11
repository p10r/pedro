package soundcloud

import (
	"github.com/alecthomas/assert/v2"
	"os"
	"testing"
)

func TestProdSoundcloud(t *testing.T) {
	t.Parallel()

	clientId := os.Getenv("SOUNDCLOUD_CLIENT_ID")
	clientSecret := os.Getenv("SOUNDCLOUD_CLIENT_SECRET")
	// This is provided via env, since it doesn't seem to be publicly exposed by SoundCloud, so this adheres to it.
	exampleArtistUrn := os.Getenv("SOUNDCLOUD_ARTIST_URN")
	if clientSecret == "" || clientId == "" || exampleArtistUrn == "" {
		t.Skip("set SOUNDCLOUD_CLIENT_SECRET, SOUNDCLOUD_CLIENT_ID and SOUNDCLOUD_ARTIST_URN to run this test")
	}

	c := MustNewClient(t, TokenUrl, ApiUrl, clientId, clientSecret)

	t.Run("fetches artist by url", func(t *testing.T) {

		res, err := c.ArtistByUrl("https://soundcloud.com/bizzarro_universe")
		assert.NoError(t, err)
		assert.Equal(t, "Bizzarro Universe", res.Username)
	})

	t.Run("finds artist by search query", func(t *testing.T) {
		res, err := c.ArtistByQuery("Anna Reusch")
		assert.NoError(t, err)
		assert.True(t, len(res) > 30)
	})
}
