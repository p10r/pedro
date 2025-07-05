package main

import (
	"context"
	"github.com/p10r/pedro/internal/telegram"
	"github.com/sethvargo/go-envconfig"
	"log"
)

type Config struct {
	TelegramToken  string  `env:"TELEGRAM_TOKEN, required"`
	TelegramAdmin  string  `env:"TELEGRAM_ADMIN_USER, required"`
	AllowedUserIds []int64 `env:"ALLOWED_USER_IDS, required"`
}

func main() {
	ctx := context.Background()

	var cfg Config
	if err := envconfig.Process(ctx, &cfg); err != nil {
		log.Fatal("Could not parse config. err: ", err)
	}

	telegram.NewPedroBot(cfg.TelegramToken, cfg.AllowedUserIds).Start()
}
