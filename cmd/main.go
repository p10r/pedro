package main

import (
	"context"
	"fmt"
	"github.com/google/uuid"
	"github.com/p10r/pedro/giftbox"
	"github.com/p10r/pedro/pedro/telegram"
	"github.com/p10r/pedro/pkg/l"
	"github.com/p10r/pedro/pkg/sqlite"
	"github.com/p10r/pedro/serve"
	gracefulshutdown "github.com/quii/go-graceful-shutdown"
	"github.com/sethvargo/go-envconfig"
	"log/slog"
	"net/http"
	"os"
	"time"
)

type Config struct {
	TelegramToken    string  `env:"TELEGRAM_TOKEN"`
	DSN              string  `env:"DSN"`
	AllowedUserIds   []int64 `env:"ALLOWED_USER_IDS"`
	FlashscoreApiKey string  `env:"FLASHSCORE_API_KEY"`
	DiscordUri       string  `env:"DISCORD_URI"`
	GiftBoxApiKey    string  `env:"GIFT_BOX_API_KEY"`
}

const serveImportUpcomingSchedule = "0 6 * * *"
const serveImportFinishedSchedule = "0 20 * * *"
const flashscoreUri = "https://flashscore.p.rapidapi.com"

var favouriteLeagues = []string{
	"Italy: SuperLega",
	"Italy: SuperLega - Play Offs",
	"Italy: Coppa Italia A1",
	"Italy: Coppa Italia A1 Women",
	"Italy: Serie A1 Women",
	"Italy: Serie A1 Women - Playoffs",
	"Poland: PlusLiga",
	"Poland: PlusLiga - Play Offs",
	"France: Ligue A - Play Offs",
	"France: Ligue A",
	"Russia: Super League - Play Offs",
	"Russia: Super League",
	"Russia: Russia Cup",
	"World: Nations League",
	"World: Nations League - Play Offs",
	"World: Nations League Women",
	"World: Nations League Women - Play Offs",
	"World: Pan-American Cup",
	"World: World Championship - First round",
	"World: World Championship - Second round",
	"World: World Championship - Play Offs",
	"World: World Championship Women - First round",
	"Germany: VBL Supercup",
	"Germany: 1. Bundesliga",
	"Germany: 1. Bundesliga - Losers stage",
	"Germany: 1. Bundesliga - Winners stage",
	"Germany: 1. Bundesliga - Play Offs",
	"Germany: DVV Cup",
	"Turkey: Sultanlar Ligi Women",
	"Turkey: Sultanlar Ligi Women - Play Offs",
	"Turkey: Efeler Ligi",
	"TURKEY: Efeler Ligi - Play Offs",
	"Turkey: Efeler Ligi - 5th-8th places",
	"Europe: Champions League",
	"Europe: Champions League Women",
	"Europe: Champions League Women - Play Offs",
	"Europe: Champions League - Play Offs",
	"Europe: CEV Cup",
	"Europe: European Championships Women",
	"Europe: European Championships",
	"JAPAN: SV.League",
}

func main() {
	jsonLogHandler := slog.NewJSONHandler(os.Stdout, nil)
	log := slog.New(jsonLogHandler)
	ctx := context.Background()

	var cfg Config
	if err := envconfig.Process(ctx, &cfg); err != nil {
		log.Error(l.Error("error loading config", err))
	}

	conn := sqlite.NewDB(cfg.DSN)
	err := conn.Open()
	if err != nil {
		log.Error(l.Error("cannot open sqlite connection", err))
		panic(err)
	}
	log.Info(fmt.Sprintf("DSN is set to %v", cfg.DSN))

	pedroApp := telegram.NewPedro(
		conn,
		cfg.TelegramToken,
		cfg.AllowedUserIds,
		jsonLogHandler,
	)

	serveApp := serve.NewServeApp(
		conn,
		flashscoreUri,
		cfg.FlashscoreApiKey,
		cfg.DiscordUri,
		favouriteLeagues,
		jsonLogHandler,
		serveImportUpcomingSchedule,
		serveImportFinishedSchedule,
	)

	// giftbox
	idGen := func() (string, error) {
		v7, err := uuid.NewV7()
		if err != nil {
			return "", err
		}
		return v7.String(), nil
	}

	httpServer := &http.Server{
		Addr:              ":8080",
		ReadHeaderTimeout: 10 * time.Second,
		Handler:           giftbox.NewServer(ctx, conn, idGen, cfg.GiftBoxApiKey),
	}
	server := gracefulshutdown.NewServer(httpServer)

	go pedroApp.Start()
	go serveApp.StartBackgroundJobs(ctx)
	if err := server.ListenAndServe(ctx); err != nil {
		log.Error(l.Error("didn't shut down gracefully", err))
	}
}
