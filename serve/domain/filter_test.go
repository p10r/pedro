package domain_test

import (
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/serve/domain"
	"github.com/p10r/pedro/serve/testutil"
	"testing"
)

func TestDomain(t *testing.T) {
	t.Run("filters for scheduled matches", func(t *testing.T) {
		expected := domain.Matches{
			{
				HomeName:         "Trentino",
				AwayName:         "Jastrzebski",
				StartTime:        1714917600,
				FlashscoreName:   "Europe: Champions League - Play Offs",
				Country:          "Europe",
				League:           "Champions League - Play Offs",
				Stage:            "SCHEDULED",
				HomeScoreCurrent: 3,
				AwayScoreCurrent: 0,
			},
			{
				HomeName:         "Resovia",
				AwayName:         "Zaksa",
				StartTime:        1714917600,
				FlashscoreName:   "Europe: Champions League - Play Offs",
				Country:          "Europe",
				League:           "Champions League - Play Offs",
				Stage:            "SCHEDULED",
				HomeScoreCurrent: 3,
				AwayScoreCurrent: 0,
			},
		}

		m := testutil.Matches(t)
		favs := []string{"Europe: Champions League - Play Offs"}
		matches := m.FilterScheduled(favs)
		assert.Equal(t, matches, expected)
	})

	t.Run("filters for finished matches", func(t *testing.T) {
		expected := domain.FinishedMatches{
			domain.FinishedMatch{
				Match: domain.Match{
					HomeName:         "Mok Mursa",
					AwayName:         "HAOK Mladost",
					StartTime:        1714932000,
					FlashscoreName:   "Croatia: Superliga - Play Offs",
					Country:          "Croatia",
					League:           "Superliga - Play Offs",
					Stage:            "FINISHED",
					HomeScoreCurrent: 2,
					AwayScoreCurrent: 3,
				},
			},
		}

		m := testutil.Matches(t)
		matches := m.FilterFinished([]string{"Croatia: Superliga - Play Offs"})
		assert.Equal(t, matches, expected)
	})

	t.Run("handles 0 scheduled matches", func(t *testing.T) {
		m := domain.Matches{}.FilterScheduled([]string{"Italy: SuperLega"})
		assert.Equal(t, len(m), 0)
	})

	t.Run("filters for favourites", func(t *testing.T) {
		expected := domain.Matches{
			{
				HomeName:         "Trentino",
				AwayName:         "Jastrzebski",
				StartTime:        1714917600,
				FlashscoreName:   "Europe: Champions League - Play Offs",
				Country:          "Europe",
				League:           "Champions League - Play Offs",
				Stage:            "SCHEDULED",
				HomeScoreCurrent: 3,
				AwayScoreCurrent: 0,
			},
			{
				HomeName:         "Resovia",
				AwayName:         "Zaksa",
				StartTime:        1714917600,
				FlashscoreName:   "Europe: Champions League - Play Offs",
				Country:          "Europe",
				League:           "Champions League - Play Offs",
				Stage:            "SCHEDULED",
				HomeScoreCurrent: 3,
				AwayScoreCurrent: 0,
			},
			{
				HomeName:         "Grand Rapids Rise W",
				AwayName:         "San Diego Mojo W",
				StartTime:        1714939200,
				FlashscoreName:   "USA: PVF Women",
				Country:          "USA",
				League:           "PVF Women",
				Stage:            "SCHEDULED",
				HomeScoreCurrent: 3,
				AwayScoreCurrent: 0,
			},
		}

		favourites := []string{"Europe: Champions League - Play Offs", "USA: PVF Women"}

		matches := testutil.Matches(t).FilterScheduled(favourites)
		assert.Equal(t, matches, expected)
	})
}
