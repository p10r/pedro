package claude

import (
	"github.com/p10r/pedro/internal"
	"regexp"
	"strings"
	"testing"
)

type Fake struct {
	t           *testing.T
	artistRegex *regexp.Regexp
}

func NewFake(t *testing.T) *Fake {
	matchArtistRegex := `https?://(?:www\.)?soundcloud\.com/[a-zA-Z0-9_-]+(?:/[a-zA-Z0-9_-]+)*(?:\?[^\s]*)?`
	re := regexp.MustCompile(matchArtistRegex)
	return &Fake{t: t, artistRegex: re}
}

// ParseCommand represents poor man's AI
// TODO add a callback parameter here to set error cases
func (f *Fake) ParseCommand(text string) (internal.ParsingResult, error) {
	text = strings.ToLower(text)

	if strings.Contains(text, "unfollow") ||
		strings.Contains(text, "stop follow") ||
		strings.Contains(text, "not follow") {

		var artist string
		switch {
		case strings.Contains(text, "hovr"):
			artist = "HOVR"
		case strings.Contains(text, "bizzarro universe"):
			artist = "Bizzarro Universe"
		default:
			f.t.Fatalf("claude.Fake Could not find artist to return")
		}

		return internal.ParsingResult{
			Command:       internal.ParsingCmdUnfollow,
			SoundcloudUrl: "",
			ArtistName:    artist,
		}, nil
	}

	if strings.Contains(text, "follow") {
		url := f.artistRegex.FindString(text)

		return internal.ParsingResult{
			Command:       internal.ParsingCmdFollow,
			SoundcloudUrl: url,
			ArtistName:    "",
		}, nil
	}

	return internal.ParsingResult{
		Command:       internal.ParsingError,
		SoundcloudUrl: "",
		ArtistName:    "",
	}, nil
}
