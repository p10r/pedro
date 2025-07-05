package telegram

import (
	"fmt"
	"gopkg.in/telebot.v4"
)

func listArtists(sender Sender) func(c telebot.Context) error {
	return func(c telebot.Context) error {
		id := c.Sender().ID
		return sender.Send(c, fmt.Sprintf("Hi %v, you are not following any artists so far", id))
	}
}
