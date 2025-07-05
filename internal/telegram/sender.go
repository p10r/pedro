package telegram

import (
	"gopkg.in/telebot.v4"
)

// Sender is a passthrough interface to be able to mock outgoing calls
type Sender interface {
	Send(c telebot.Context, msg string) error
}

//goland:noinspection GoNameStartsWithPackageName
type TelegramSender struct {
}

func NewTelegramSender() *TelegramSender {
	return &TelegramSender{}
}

func (s TelegramSender) Send(c telebot.Context, msg string) error {
	return c.Send(msg)
}
