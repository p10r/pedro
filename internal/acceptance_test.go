package internal_test

import (
	"context"
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/internal"
	"os"
	"testing"
)

func TestFollowingArtists(t *testing.T) {
	var testEnv func() *internal.Pedro

	clientId := os.Getenv("SOUNDCLOUD_CLIENT_ID")
	clientSecret := os.Getenv("SOUNDCLOUD_CLIENT_SECRET")
	claudeApiKey := os.Getenv("CLAUDE_API_KEY")
	if clientSecret == "" || clientId == "" || claudeApiKey == "" {
		t.Log("running in in-memory mode")
		testEnv = func() *internal.Pedro { return mustNewInMemoryTestEnv(t) }
	} else {
		t.Log("running in integration mode")
		testEnv = func() *internal.Pedro { return mustNewIntegrationTestEnv(t, clientId, clientSecret, claudeApiKey) }
	}

	t.Run("follow an artist", func(t *testing.T) {
		t.Parallel()
		ctx := context.Background()
		pedro := testEnv()

		userId := internal.UserId(44124)
		cmd := internal.FollowArtistCmd{SoundcloudUrl: "https://soundcloud.com/hovrmusic", UserId: userId}

		_, err := pedro.FollowArtist(ctx, cmd)
		assert.NoError(t, err)

		res, err := pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)

		expected := internal.Artists{{
			Name: "HOVR",
			Url:  "https://soundcloud.com/hovrmusic",
		}}
		assert.Equal(t, expected, res)
	})

	t.Run("follow an artist through free text", func(t *testing.T) {
		t.Parallel()
		ctx := context.Background()
		pedro := testEnv()

		userId := internal.UserId(44124)
		success, err := pedro.ParseAndExecute(ctx, "Hi, I want to follow https://soundcloud.com/bizzarro_universe", userId)
		assert.NoError(t, err)
		assert.Equal(t, "You're now following Bizzarro Universe", success)

		res, err := pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)

		expected := internal.Artists{{
			Name: "Bizzarro Universe",
			Url:  "https://soundcloud.com/bizzarro_universe",
		}}
		assert.Equal(t, expected, res)
	})

	t.Run("try following an artist that doesn't exist", func(t *testing.T) {
		t.Parallel()
		ctx := context.Background()
		pedro := testEnv()

		userId := internal.UserId(44124)
		cmd := internal.FollowArtistCmd{SoundcloudUrl: "https://soundcloud.com/dkwpjaiodwoadboaiwd", UserId: userId}

		_, err := pedro.FollowArtist(ctx, cmd)
		assert.Error(t, err)
	})

	t.Run("try following the same artist twice", func(t *testing.T) {
		t.Parallel()
		ctx := context.Background()
		pedro := testEnv()

		userId := internal.UserId(44124)
		cmd := internal.FollowArtistCmd{SoundcloudUrl: "https://soundcloud.com/hovrmusic", UserId: userId}

		_, err := pedro.FollowArtist(ctx, cmd)
		assert.NoError(t, err)
		_, err = pedro.FollowArtist(ctx, cmd)
		assert.NoError(t, err)

		res, err := pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)
		expected := internal.Artists{{
			Name: "HOVR",
			Url:  "https://soundcloud.com/hovrmusic",
		}}
		assert.Equal(t, expected, res)
	})

	t.Run("unfollow artist", func(t *testing.T) {
		t.Parallel()
		ctx := context.Background()
		pedro := testEnv()

		userId := internal.UserId(1241)
		bizzarro := internal.FollowArtistCmd{SoundcloudUrl: "https://soundcloud.com/bizzarro_universe", UserId: userId}
		hovr := internal.FollowArtistCmd{SoundcloudUrl: "https://soundcloud.com/hovrmusic", UserId: userId}

		_, err := pedro.FollowArtist(ctx, hovr)
		assert.NoError(t, err)
		_, err = pedro.FollowArtist(ctx, bizzarro)
		assert.NoError(t, err)

		res, err := pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)
		assert.Equal(t, "HOVR", res[0].Name)

		unfollowCmd := internal.UnfollowArtistCmd{
			ArtistName: "HOVR",
			UserId:     userId,
		}
		name, err := pedro.UnfollowArtist(ctx, unfollowCmd)
		assert.NoError(t, err)
		assert.Equal(t, "HOVR", name)

		res, err = pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)
		assert.Equal(t, internal.Artists{{
			Name: "Bizzarro Universe",
			Url:  "https://soundcloud.com/bizzarro_universe",
		}}, res)
	})

	t.Run("unfollow an artist through free text", func(t *testing.T) {
		t.Parallel()
		ctx := context.Background()
		pedro := testEnv()

		userId := internal.UserId(44124)
		_, err := pedro.ParseAndExecute(ctx, "Hi, I want to follow https://soundcloud.com/bizzarro_universe", userId)
		assert.NoError(t, err)

		artists, err := pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)
		assert.Equal(t, 1, len(artists))

		success, err := pedro.ParseAndExecute(ctx, "Hi, I want to stop following Bizzarro Universe", userId)
		assert.NoError(t, err)
		assert.Equal(t, "You stopped following Bizzarro Universe", success)

		artists, err = pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)
		assert.Equal(t, 0, len(artists))
	})

	t.Run("try unfollowing an artist that is not in user's followed artists", func(t *testing.T) {
		t.Parallel()
		ctx := context.Background()
		pedro := testEnv()
		userId := internal.UserId(1241)

		bizzarro := internal.FollowArtistCmd{SoundcloudUrl: "https://soundcloud.com/bizzarro_universe", UserId: userId}
		_, err := pedro.FollowArtist(ctx, bizzarro)
		assert.NoError(t, err)

		res, err := pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)
		assert.Equal(t, 1, len(res))

		cmd := internal.UnfollowArtistCmd{
			ArtistName: "UNKNOWN",
			UserId:     userId,
		}
		_, err = pedro.UnfollowArtist(ctx, cmd)
		assert.Error(t, err)

		res, err = pedro.ListArtists(ctx, userId)
		assert.NoError(t, err)
		assert.Equal(t, 1, len(res))
	})

}
