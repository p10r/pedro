package telegram

import (
	"gopkg.in/telebot.v4"
	"gopkg.in/telebot.v4/middleware"
	"log"
	"time"
)

func NewPedroBot(botToken string, allowedUserIds []int64) *telebot.Bot {
	bot, err := telebot.NewBot(
		telebot.Settings{
			Token:   botToken,
			Poller:  &telebot.LongPoller{Timeout: 10 * time.Second},
			Verbose: false,
		},
	)
	if err != nil {
		log.Fatal("Cannot create telegram bot")
	}

	sender := NewTelegramSender()

	bot.Use(middleware.Whitelist(allowedUserIds...))
	bot.Handle("/follow", listArtists(sender))

	return bot
}
