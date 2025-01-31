package flashscore_test

import (
	"bytes"
	"github.com/alecthomas/assert/v2"
	"github.com/p10r/pedro/pedro/domain/expect"
	"github.com/p10r/pedro/pkg/l"
	"github.com/p10r/pedro/serve/domain"
	"github.com/p10r/pedro/serve/flashscore"
	"github.com/p10r/pedro/serve/testutil"
	"io"
	"net/http"
	"net/http/httptest"
	"testing"
)

// TODO move to testutil
func NewFakeServer(t *testing.T, apiKey string) *httptest.Server {
	t.Helper()

	return httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != "GET" {
			w.WriteHeader(400)
			return
		}

		reqApiKey := r.Header.Get("X-RapidAPI-Key")
		if reqApiKey != apiKey {
			w.WriteHeader(400)
			return
		}

		json := testutil.RawFlashscoreRes(t)
		_, _ = w.Write(json)
	}))
}

func TestFlashscore(t *testing.T) {
	log := l.NewTextLogger()

	t.Run("deserializes flashscore response", func(t *testing.T) {
		json := testutil.RawFlashscoreRes(t)

		response, err := flashscore.NewResponse(io.NopCloser(bytes.NewBufferString(string(json))))

		assert.NoError(t, err)
		assert.Equal(t, len(response.Leagues), 6)
	})

	t.Run("fetches response", func(t *testing.T) {
		apiKey := "1234"
		flashscoreServer := NewFakeServer(t, apiKey)
		defer flashscoreServer.Close()

		client := flashscore.NewClient(flashscoreServer.URL, apiKey, log)

		_, err := client.GetUpcomingMatches()
		assert.NoError(t, err)
	})

	t.Run("reports error", func(t *testing.T) {
		flashscoreServer := httptest.NewServer(http.HandlerFunc(
			func(w http.ResponseWriter, r *http.Request) {
				w.WriteHeader(400)
			}))
		defer flashscoreServer.Close()

		client := flashscore.NewClient(flashscoreServer.URL, "apiKey", log)

		_, err := client.GetUpcomingMatches()
		expect.Err(t, err)
	})

	t.Run("map flashscore response to flashscore matches", func(t *testing.T) {
		resp := flashscore.Response{
			Leagues: flashscore.Leagues{flashscore.League{
				Name: "Germany: 1. Bundesliga",
				Events: flashscore.Events{
					flashscore.Event{
						HomeName:         "Berlin",
						AwayName:         "Haching",
						StartTime:        1234,
						HomeScoreCurrent: "3",
						AwayScoreCurrent: "1",
						Stage:            "FINISHED",
					},
				},
			}}}

		expected := domain.Matches{
			domain.Match{
				HomeName:         "Berlin",
				AwayName:         "Haching",
				StartTime:        1234,
				FlashscoreName:   "Germany: 1. Bundesliga",
				Country:          "Germany",
				League:           "1. Bundesliga",
				Stage:            "FINISHED",
				HomeScoreCurrent: 3,
				AwayScoreCurrent: 1,
			},
		}

		assert.Equal(t, resp.ToMatches(), expected)
	})

}
