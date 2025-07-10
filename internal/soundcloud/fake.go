package soundcloud

import (
	"github.com/p10r/pedro/internal"
	"testing"
)

type Fake struct {
	responses FakeResponses
}

type FakeResponses struct {
	resolveEndpoint map[string]internal.SoundcloudArtist
}

func NewInMemoryClient(t *testing.T, responses FakeResponses) *Fake {
	t.Helper()

	return &Fake{responses: responses}
}

func (c Fake) ArtistByUrl(url string) (internal.SoundcloudArtist, error) {
	return c.responses.resolveEndpoint[url], nil
}
