package claude

import (
	"github.com/p10r/pedro/internal"
	"regexp"
	"strings"
	"testing"
)

type Fake struct {
	t *testing.T
}

func NewFake(t *testing.T) *Fake {
	return &Fake{t: t}
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

	// Follow by url
	urlMatcher := `https?://(?:www\.)?soundcloud\.com/[a-zA-Z0-9_-]+(?:/[a-zA-Z0-9_-]+)*(?:\?[^\s]*)?`
	urlRegex := regexp.MustCompile(urlMatcher)
	if strings.Contains(text, "follow") && strings.Contains(text, "http") {
		url := urlRegex.FindString(text)

		return internal.ParsingResult{
			Command:       internal.ParsingCmdFollow,
			SoundcloudUrl: url,
			ArtistName:    "",
		}, nil
	}

	pattern := `(?i)hovr|sinamin|anna reusch|bizzarro universe`
	artistNameRegex := regexp.MustCompile(pattern)

	// Follow by name
	if strings.Contains(text, "follow") && artistNameRegex.MatchString(text) {
		return internal.ParsingResult{
			Command:       internal.ParsingCmdFollow,
			SoundcloudUrl: "",
			ArtistName:    artistNameRegex.FindString(text),
		}, nil
	}

	return internal.ParsingResult{
		Command:       internal.ParsingError,
		SoundcloudUrl: "",
		ArtistName:    "",
	}, nil
}
