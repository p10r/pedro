package soundcloud

import (
	"fmt"
	"github.com/p10r/pedro/internal"
	"strings"
	"testing"
)

type Fake struct {
}

func NewFakeClient(t *testing.T) *Fake {
	t.Helper()
	return &Fake{}
}

func (c Fake) ArtistByUrl(url string) (internal.SoundcloudArtist, error) {
	artist := resolveUrl[url]
	if artist == (internal.SoundcloudArtist{}) {
		return internal.SoundcloudArtist{}, fmt.Errorf("cannot find artist %s", url)
	}
	return artist, nil
}

func (c Fake) ArtistByQuery(query string) ([]internal.SoundcloudArtist, error) {
	hits := queryUrl[strings.ToLower(query)]
	if len(hits) == 0 {
		return []internal.SoundcloudArtist{}, fmt.Errorf("cannot find hits for %s", query)
	}
	return hits, nil
}

var (
	resolveUrl = map[string]internal.SoundcloudArtist{
		"https://soundcloud.com/bizzarro_universe": {
			Id:       1,
			Urn:      "soundcloud:users:1",
			Url:      "https://soundcloud.com/bizzarro_universe",
			Username: "Bizzarro Universe",
		},
		"https://soundcloud.com/hovrmusic": {
			Id:       2,
			Urn:      "soundcloud:users:2",
			Url:      "https://soundcloud.com/hovrmusic",
			Username: "HOVR",
		},
	}
	queryUrl = map[string][]internal.SoundcloudArtist{
		"anna reusch": {
			{
				Id:             24337241,
				Urn:            "soundcloud:users:users/hovr-1",
				Url:            "https://api.soundcloud.com/users/anna-r-1",
				Username:       "Anna Kreusch",
				FollowersCount: 3,
			},
			{
				Id:             359162246,
				Urn:            "soundcloud:users:anna-r-2",
				Url:            "https://api.soundcloud.com/users/anna-r-2",
				Username:       "Anna Reusch",
				FollowersCount: 3331,
			},
			{
				Id:             1368098883,
				Urn:            "soundcloud:users:anna-r-3",
				Url:            "https://api.soundcloud.com/users/anna-r-3",
				Username:       "Anna Reusche",
				FollowersCount: 0,
			},
			{
				Id:             644747196,
				Urn:            "soundcloud:users:anna-r-4",
				Url:            "https://api.soundcloud.com/users/anna-r-4",
				Username:       "Anna Rausch",
				FollowersCount: 0,
			},
			{
				Id:             777803923,
				Urn:            "soundcloud:users:anna-r-5",
				Url:            "https://api.soundcloud.com/users/anna-r-5",
				Username:       "ANNA RAUSCH",
				FollowersCount: 0,
			},
			{
				Id:             1247001895,
				Urn:            "soundcloud:users:anna-r-6",
				Url:            "https://api.soundcloud.com/users/anna-r-6",
				Username:       "Anna Rausch",
				FollowersCount: 0,
			},
			{
				Id:             1542350157,
				Urn:            "soundcloud:users:anna-r-7",
				Url:            "https://api.soundcloud.com/users/anna-r-7",
				Username:       "Anna Reisch",
				FollowersCount: 0,
			},
		},
		"hovr": {
			{
				Id:             34762414,
				Urn:            "soundcloud:users:hovr-1",
				Url:            "https://soundcloud.com/hovrmusic",
				Username:       "HOVR",
				FollowersCount: 11640,
			},
			{
				Id:             976193461,
				Urn:            "soundcloud:users:hovr-2",
				Url:            "https://api.soundcloud.com/users/hovr-2",
				Username:       "HOVR",
				FollowersCount: 3,
			},
			{
				Id:             312751490,
				Urn:            "soundcloud:users:hovr-3",
				Url:            "https://api.soundcloud.com/users/hovr-3",
				Username:       "HOVR Ceilidh Band",
				FollowersCount: 1,
			},
		},
		"bizzarro universe": {
			{
				Id:             148907910,
				Urn:            "soundcloud:users:biz-2",
				Url:            "https://api.soundcloud.com/users/biz-2",
				Username:       "edible universe",
				FollowersCount: 6,
			},
			{
				Id:             550473735,
				Urn:            "soundcloud:users:biz-1",
				Url:            "https://soundcloud.com/bizzarro_universe",
				Username:       "Bizzarro Universe",
				FollowersCount: 3669,
			},
		},
	}
)
