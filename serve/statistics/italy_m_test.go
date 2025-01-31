package statistics

import (
	"cmp"
	"fmt"
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/serve/testutil"
	"net/http"
	"slices"
	"strconv"
	"testing"
)

func TestSuperLegaScraper(t *testing.T) {

	t.Run("parses all matches with results", func(t *testing.T) {
		s := newSuperLegaScraper("", nil)
		f := testutil.MustReadFile(t, "../testdata/statistics/superlega-italy-m.html")
		res := testutil.OkRes(f)

		stats, err := s.parseStats(res)
		assert.NoError(t, err)

		expected := []superLegaMatch{
			{"6551", "6555", "http://www.legavolley.it/match/38274"},
			{"6552", "6559", "http://www.legavolley.it/match/38279"},
			{"6553", "6551", "http://www.legavolley.it/match/38278"},
			{"6554", "6552", "http://www.legavolley.it/match/38285"},
			{"6554", "6557", "http://www.legavolley.it/match/38277"},
			{"6555", "6556", "http://www.legavolley.it/match/38276"},
			{"6556", "6551", "http://www.legavolley.it/match/38283"},
			{"6557", "6592", "http://www.legavolley.it/match/38275"},
			{"6557", "6553", "http://www.legavolley.it/match/38282"},
			{"6558", "6555", "http://www.legavolley.it/match/38286"},
			{"6559", "6560", "http://www.legavolley.it/match/38270"},
			{"6559", "6592", "http://www.legavolley.it/match/38287"},
			{"6560", "6561", "http://www.legavolley.it/match/38284"},
			{"6560", "6558", "http://www.legavolley.it/match/38281"},
			{"6561", "6554", "http://www.legavolley.it/match/38271"},
			{"6592", "6561", "http://www.legavolley.it/match/38280"},
		}

		var actual []superLegaMatch
		for key := range stats {
			actual = append(actual, stats[key])
		}

		fmt.Printf("actual: %+v\n\n", sortedByStatUrl(actual))
		fmt.Printf("expected: %+v", sortedByStatUrl(expected))

		assert.Equal(t, sortedByStatUrl(expected), sortedByStatUrl(actual))
	})

	t.Run("run against prod", func(t *testing.T) {
		t.Skip()

		s := newSuperLegaScraper("https://www.legavolley.it", &http.Client{})

		res, err := s.getAllMatchesPage()
		assert.NoError(t, err)

		matches, err := s.parseStats(res)
		assert.NoError(t, err)
		assert.Equal(t, 16, len(matches))
	})
}

func sortedByStatUrl(m []superLegaMatch) []superLegaMatch {
	slices.SortFunc(m, func(a, b superLegaMatch) int {
		first, _ := strconv.Atoi(superLegaUrlRegex.FindString(a.statsUrl))
		second, _ := strconv.Atoi(superLegaUrlRegex.FindString(b.statsUrl))
		return cmp.Compare(first, second)
	})
	return m
}
