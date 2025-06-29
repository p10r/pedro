package soundcloud

import (
	"errors"
	"github.com/alecthomas/assert/v2"
	"os"
	"testing"
)

func TestSoundcloud(t *testing.T) {
	secret := os.Getenv("SOUNDCLOUD_API_SECRET")
	if secret == "" {
		t.Skip("set SOUNDCLOUD_API_SECRET to run this test")
	}

	const (
		authUrl = "https://secure.soundcloud.com/"
		apiUrl  = "https://api.soundcloud.com/"
	)

	t.Run("fetches auth token", func(t *testing.T) {
		c := mustNewClient(t, authUrl, apiUrl, secret)

		res, err := c.Authorize()

		assert.NoError(t, err)
		assert.NotZero(t, res.AccessToken, "Did not expect zero value for AccessToken")
		assert.NotZero(t, res.TokenType, "Did not expect zero value for TokenType")
		assert.NotZero(t, res.ExpiresIn, "Did not expect zero value for ExpiresIn")
		assert.NotZero(t, res.RefreshToken, "Did not expect zero value for RefreshToken")
		assert.Zero(t, res.Scope)
	})

	t.Run("fetches artist by url", func(t *testing.T) {
		//c := mustNewClient(t, authUrl, apiUrl, secret)

		//res, err := c.ArtistByUrl("https://soundcloud.com/bizzarro_universe")
	})

	t.Run("fetches artist by urn", func(t *testing.T) {

	})

	t.Run("err on outdated OAuth token", func(t *testing.T) {

	})

	t.Run("returns err when unauthorized", func(t *testing.T) {
		c := mustNewClient(t, authUrl, apiUrl, "wahhhh=")

		_, err := c.Authorize()

		var unAuthedErr *unauthorizedErr
		if !errors.As(err, &unAuthedErr) {
			t.Fatal()
		}
	})

	t.Run("returns err on invalid input urls", func(t *testing.T) {

	})

}

func mustNewClient(t *testing.T, authUrl, apiUrl, secret string) *client {
	c, err := newClient(authUrl, apiUrl, secret)
	if err != nil {
		t.Fatal("could not create client")
	}
	return c
}
