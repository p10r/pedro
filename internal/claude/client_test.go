package claude_test

import (
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/internal"
	"github.com/p10r/pedro/internal/claude"
	"os"
	"testing"
)

// This test will burn through your Claude API tokens, so it is disabled by default.
func TestClaudeIntegration(t *testing.T) {
	apiKey := os.Getenv("CLAUDE_API_KEY")
	if apiKey == "" {
		t.Skip("set CLAUDE_API_KEY to run this test")
	}

	client := claude.NewClient(claude.ApiUrl, apiKey)

	t.Run("parses a follow command", func(t *testing.T) {
		input := "Hey, I'd like to follow https://soundcloud.com/bizzarro_universe on soundcloud. Thanks!"
		res, err := client.ParseCommand(input)
		assert.NoError(t, err)

		expected := internal.ParsingResult{
			Command:       "FOLLOW",
			SoundcloudUrl: "https://soundcloud.com/bizzarro_universe",
			ArtistName:    "",
		}
		assert.Equal(t, expected, res)
	})

	t.Run("parses an unfollow command", func(t *testing.T) {
		input := "Please unfollow Bizzarro Universe"
		res, err := client.ParseCommand(input)
		assert.NoError(t, err)

		expected := internal.ParsingResult{
			Command:       "UNFOLLOW",
			SoundcloudUrl: "",
			ArtistName:    "Bizzarro Universe",
		}
		assert.Equal(t, expected, res)
	})

	t.Run("returns parsing error on unparseable input", func(t *testing.T) {
		input := "Hey, I'd just like to talk."
		res, err := client.ParseCommand(input)
		assert.NoError(t, err)

		expected := internal.ParsingResult{
			Command:       "PARSING_ERROR",
			SoundcloudUrl: "",
			ArtistName:    "",
		}
		assert.Equal(t, expected, res)
	})
}
