package serve

import (
	"context"
	"github.com/p10r/pedro/pkg/sqlite"
	"github.com/p10r/pedro/serve/db"
	"github.com/p10r/pedro/serve/discord"
	"github.com/p10r/pedro/serve/domain"
	"github.com/p10r/pedro/serve/flashscore"
	"github.com/robfig/cron/v3"
	"log/slog"
	"time"
)

type Serve struct {
	Importer *domain.MatchImporter
	log      *slog.Logger
}

// NewServeApp wires Serve App together.
// Expects an already opened connection.
func NewServeApp(
	conn *sqlite.DB,
	flashscoreUri, flashscoreApiKey, discordUri string,
	favouriteLeagues []string,
	logHandler slog.Handler,
) *Serve {
	log := slog.New(logHandler).With(slog.String("app", "serve"))

	log.Info("Starting Serve App")

	if flashscoreUri == "" {
		log.Error("flashscoreUri has not been set")
	}
	if flashscoreApiKey == "" {
		log.Error("flashscoreApiKey has not been set")
	}
	if discordUri == "" {
		log.Error("DISCORD_URI has not been set")
	}

	store := db.NewMatchStore(conn)
	flashscoreClient := flashscore.NewClient(flashscoreUri, flashscoreApiKey, log)
	discordClient := discord.NewClient(discordUri, log)
	now := func() time.Time { return time.Now() }

	importer := domain.NewMatchImporter(
		store,
		flashscoreClient,
		discordClient,
		favouriteLeagues,
		now,
		log,
	)

	return &Serve{importer, log}
}

func (s *Serve) Start(ctx context.Context) {
	c := cron.New()
	_, err := c.AddFunc("* * * * *", func() {
		_, err := s.Importer.ImportScheduledMatches(ctx)
		if err != nil {
			s.log.Error("serve run failed", slog.Any("error", err))
		}
	})
	if err != nil {
		s.log.Error("serve run failed", slog.Any("error", err))
	}

	for {
		select {
		case <-ctx.Done():
			return
		default:
			c.Start()
		}
	}
}
