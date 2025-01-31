package domain

import (
	"context"
	"fmt"
	"github.com/p10r/pedro/pkg/l"
	"log/slog"
	"time"
)

type MatchImporter struct {
	flashscore Flashscore
	discord    Discord
	statistics Statistics
	clock      func() time.Time
	log        *slog.Logger
}

func NewMatchImporter(
	flashscore Flashscore,
	discord Discord,
	statistics Statistics,
	clock func() time.Time,
	log *slog.Logger,
) *MatchImporter {
	return &MatchImporter{
		flashscore,
		discord,
		statistics,
		clock,
		log,
	}
}

// ImportScheduledMatches writes matches from flashscore into the db for the current day.
// Doesn't validate if the match is already present,
// as it's expected to be triggered only once per day for now.
func (importer *MatchImporter) ImportScheduledMatches(ctx context.Context) (Matches, error) {
	matches, err := importer.fetchAllMatches()
	if err != nil {
		importer.log.Error(l.Error("cannot fetch matches", err))
		return nil, err
	}

	//TODO remove error, return empty slice
	upcoming := matches.Favourites().Scheduled()
	if len(upcoming) == 0 {
		importer.log.Info("No upcoming games today")
		return Matches{}, nil
	}

	err = importer.discord.SendUpcomingMatches(ctx, upcoming, importer.clock())
	if err != nil {
		importer.log.Error(l.Error("send to discord error", err))
		return nil, err
	}

	return upcoming, nil
}

func (importer *MatchImporter) ImportFinishedMatches(ctx context.Context) error {
	flashscoreMatches, err := importer.fetchAllMatches()
	if err != nil {
		importer.log.Error(l.Error("cannot fetch matches", err))
		return err
	}

	finished := flashscoreMatches.Favourites().Finished()
	if len(finished) == 0 {
		importer.log.Info("No finished games today")
		return nil
	}

	italianMenStats := importer.statistics.GetItalianMenStats()
	polandMenStats := importer.statistics.GetPolishMenStats()
	finished = finished.ZipWith(append(polandMenStats, italianMenStats...))

	err = importer.discord.SendFinishedMatches(ctx, finished.ToMap(), importer.clock())
	if err != nil {
		importer.log.Error(l.Error("send to discord error", err))
		return err
	}

	return nil
}

func (importer *MatchImporter) fetchAllMatches() (Matches, error) {
	m, err := importer.flashscore.GetUpcomingMatches()
	if err != nil {
		return nil, fmt.Errorf("could not fetch matches from flashscore: err: %v", err)
	}
	return m, err
}
