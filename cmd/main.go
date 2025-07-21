package main

import (
	"context"
	"github.com/p10r/pedro/internal"
	"github.com/p10r/pedro/internal/claude"
	"github.com/p10r/pedro/internal/soundcloud"
	"github.com/p10r/pedro/internal/telegram"
	"github.com/sethvargo/go-envconfig"
	"log"
)

type Config struct {
	TelegramToken      string  `env:"TELEGRAM_TOKEN, required"`
	TelegramAdmin      string  `env:"TELEGRAM_ADMIN_USER, required"`
	AllowedUserIds     []int64 `env:"ALLOWED_USER_IDS, required"`
	DbFilePath         string  `env:"DB_FILE_PATH, required"`
	SoundcloudClientId string  `env:"SOUNDCLOUD_CLIENT_ID, required"`
	SoundcloudSecret   string  `env:"SOUNDCLOUD_CLIENT_SECRET, required"`
	ClaudeApiKey       string  `env:"CLAUDE_API_KEY"`
}

func main() {
	ctx := context.Background()

	var cfg Config
	if err := envconfig.Process(ctx, &cfg); err != nil {
		log.Fatal("Could not parse config. err: ", err)
	}

	pedro := newProdPedro(cfg)
	bot := telegram.NewBot(cfg.TelegramToken, cfg.AllowedUserIds, pedro)

	bot.Start()
}

func newProdPedro(cfg Config) *internal.Pedro {
	db, err := internal.NewJsonDb(cfg.DbFilePath)
	if err != nil {
		log.Fatalf("Error when creating db: %s", err)
	}

	scOAuth, err := soundcloud.NewOAuthConfig(cfg.SoundcloudClientId, cfg.SoundcloudSecret, soundcloud.TokenUrl)
	if err != nil {
		log.Fatalf("Error when creating soundcloud ouath config: %s", err)
	}

	sc, err := soundcloud.NewClient(scOAuth, soundcloud.ApiUrl)
	if err != nil {
		log.Fatalf("Error when creating soundcloud client: %s", err)
	}

	ai := claude.NewClient(claude.ApiUrl, cfg.ClaudeApiKey)

	return internal.NewPedro(cfg.AllowedUserIds, db, sc, ai)
}
