package telegram

import (
	"context"
	"fmt"
	"gopkg.in/telebot.v3"
	"log"
	"pedro-go/domain"
)

//nolint:lll
var invalidArtistMsg = "Could not parse input, make sure to send it as follows: https://ra.co/dj/dj123"

func artistFollowedMsg(artist string) string {
	return fmt.Sprintf("You're now following %s", artist)
}

func followArtist(r *domain.ArtistRegistry) func(c telebot.Context) error {
	return func(c telebot.Context) error {
		ctx := context.Background() //TODO check if telebot can provide context

		tags := c.Args()
		if len(tags) == 0 {
			return c.Send(invalidArtistMsg)
		}

		slug, err := domain.NewSlug(tags[0])
		if err != nil {
			log.Print(err)
			return c.Send(invalidArtistMsg)
		}
		userId := domain.UserID(c.Sender().ID)

		err = r.Follow(ctx, slug, userId)
		if err != nil {
			log.Print(err)
			return c.Send(genericErrMsg("/follow", err))
		}

		return c.Send(artistFollowedMsg(string(slug)))
	}
}
