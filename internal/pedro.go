package internal

import (
	"context"
	"fmt"
	"log"
)

type Pedro struct {
	service *service
	claude  Claude
}

func NewPedro(db *JsonDb, client Soundcloud, claude Claude) *Pedro {
	return &Pedro{
		service: newService(db, client),
		claude:  claude,
	}
}

func (pedro *Pedro) ParseAndExecute(ctx context.Context, text string, userId UserId) (string, error) {
	command, err := pedro.claude.ParseCommand(text)
	if err != nil {
		return "", err
	}

	switch command.Command {
	case "FOLLOW":
		c := FollowArtistCmd{
			SoundcloudUrl: command.SoundcloudUrl,
			UserId:        userId,
		}
		success, err := pedro.FollowArtist(ctx, c)
		if err != nil {
			log.Printf("err when trying to follow artist: %s", err)
		}
		return fmt.Sprintf("You're now following %s", success), nil

	case "UNFOLLOW":
		c := UnfollowArtistCmd{
			ArtistName: command.ArtistName,
			UserId:     userId,
		}
		success, err := pedro.UnfollowArtist(ctx, c)
		if err != nil {
			log.Printf("err when trying to unfollow artist: %s", err)
		}
		return fmt.Sprintf("You stopped following %s", success), nil

	case "PARSING_ERROR":
		return "", fmt.Errorf("we're sorry, your input could not be parsed")

	default:
		log.Printf("internal.pedro: command.Command returned unexpected %s", command.Command)
		return "", fmt.Errorf("looks like we have technical difficulties. Sorry for the hiccup")
	}
}

func (pedro *Pedro) ListArtists(ctx context.Context, cmd ListArtistsCmd) (Artists, error) {
	return pedro.service.ListArtists(ctx, cmd)
}

func (pedro *Pedro) FollowArtist(ctx context.Context, cmd FollowArtistCmd) (string, error) {
	return pedro.service.FollowArtist(ctx, cmd)
}

func (pedro *Pedro) UnfollowArtist(ctx context.Context, cmd UnfollowArtistCmd) (artistName string, err error) {
	return pedro.service.UnfollowArtist(ctx, cmd)
}
