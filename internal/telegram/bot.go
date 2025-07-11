package telegram

import (
	"context"
	"github.com/p10r/pedro/internal"
	"gopkg.in/telebot.v4"
	"gopkg.in/telebot.v4/middleware"
	"log"
	"time"
)

func NewBot(botToken string, allowedUserIds []int64, pedro *internal.Pedro) *telebot.Bot {
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

	bot.Use(middleware.Whitelist(allowedUserIds...))
	bot.Handle(telebot.OnText, func(c telebot.Context) error {
		// TODO check
		ctx := context.Background()
		// All the text messages that weren't
		// captured by existing handlers.

		var (
			user = c.Sender()
			text = c.Text()
		)

		success, err := pedro.ParseAndExecute(ctx, text, internal.UserId(user.ID))
		if err != nil {
			_ = c.Send(err)
			return err
		}

		return c.Send(success)
	})

	return bot
}
