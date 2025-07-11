package soundcloud

import (
	"fmt"
	"github.com/p10r/pedro/internal"
	"testing"
)

type Fake struct {
	responses FakeResponses
}

type FakeResponses struct {
	resolveEndpoint map[string]internal.SoundcloudArtist
}

func NewFakeClient(t *testing.T) *Fake {
	t.Helper()

	resolveUrl := map[string]internal.SoundcloudArtist{
		"https://soundcloud.com/bizzarro_universe": {
			Id:       1,
			Urn:      "soundcloud:users:1",
			Url:      "https://soundcloud.com/bizzarro_universe",
			Username: "Bizzarro Universe",
		},
		"https://soundcloud.com/hovrmusic": {
			Id:       2,
			Urn:      "soundcloud:users:2",
			Url:      "https://soundcloud.com/hovrmusic",
			Username: "HOVR",
		},
	}

	responses := FakeResponses{resolveEndpoint: resolveUrl}

	return &Fake{responses: responses}
}

func (c Fake) ArtistByUrl(url string) (internal.SoundcloudArtist, error) {
	artist := c.responses.resolveEndpoint[url]
	if artist == (internal.SoundcloudArtist{}) {
		return internal.SoundcloudArtist{}, fmt.Errorf("cannot find artist %s", url)
	}
	return artist, nil
}
