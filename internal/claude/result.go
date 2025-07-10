package claude

import (
	"fmt"
	"github.com/p10r/pedro/internal"
)

type result struct {
	Command       string `json:"command_type"`
	SoundcloudUrl string `json:"soundcloud_url"`
	ArtistName    string `json:"artist_name"`
}

func (r result) toParsingResult() (internal.ParsingResult, error) {
	cmd := r.Command

	if cmd != internal.ParsingCmdFollow &&
		cmd != internal.ParsingCmdUnfollow &&
		cmd != internal.ParsingError {
		return internal.ParsingResult{}, fmt.Errorf("unknown parsing command: %s", r.Command)
	}

	return internal.ParsingResult{
		Command:       cmd,
		SoundcloudUrl: r.SoundcloudUrl,
		ArtistName:    r.ArtistName,
	}, nil
}
