package domain_test

import (
	"context"
	"pedro-go/db"
	. "pedro-go/domain"
	"pedro-go/domain/expect"
	"pedro-go/ra"
	"testing"
	"time"
)

func NewInMemoryArtistRegistry(
	raArtists ra.ArtistStore,
	now func() time.Time,
) (*ArtistRegistry, EventMonitor) {
	repo := db.NewInMemoryArtistRepository()
	m := db.NewInMemoryEventMonitor()
	raClient := ra.NewInMemoryClient(raArtists)

	return NewArtistRegistry(repo, raClient, m, now), m
}

func TestArtistRegistry(t *testing.T) {
	ctx := context.Background()
	now := func() time.Time {
		return time.Now()
	}

	t.Run("lists all artists", func(t *testing.T) {
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{
				"boysnoize": {
					Artist:     ra.Artist{RAID: "943", Name: "Boys Noize"},
					EventsData: []ra.Event{},
				},
				"sinamin": {
					Artist:     ra.Artist{RAID: "222", Name: "Sinamin"},
					EventsData: []ra.Event{},
				},
			},
			now,
		)

		err := registry.Follow(ctx, "boysnoize", UserID(1))
		expect.NoErr(t, err)

		err = registry.Follow(ctx, "sinamin", UserID(1))
		expect.NoErr(t, err)

		got, err := registry.All(ctx)
		expect.NoErr(t, err)
		want := Artists{
			{
				ID:            1,
				RAID:          "943",
				RASlug:        "boysnoize",
				Name:          "Boys Noize",
				FollowedBy:    UserIDs{UserID(1)},
				TrackedEvents: EventIDs{},
			},
			{
				ID:            2,
				RAID:          "222",
				RASlug:        "sinamin",
				Name:          "Sinamin",
				FollowedBy:    UserIDs{UserID(1)},
				TrackedEvents: EventIDs{},
			},
		}

		expect.NoErr(t, err)
		expect.DeepEqual(t, got, want)
	})

	t.Run("follows an artist from resident advisor", func(t *testing.T) {
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{
				"daftpunk": {
					Artist:     ra.Artist{RAID: "111", Name: "Daft Punk"},
					EventsData: []ra.Event{},
				},
			},
			now,
		)

		want := []Artist{
			{
				ID:            1,
				RAID:          "111",
				RASlug:        "daftpunk",
				Name:          "Daft Punk",
				FollowedBy:    UserIDs{UserID(1)},
				TrackedEvents: EventIDs{},
			}}
		err := registry.Follow(ctx, "daftpunk", UserID(1))

		expect.NoErr(t, err)
		all, err := registry.All(ctx)
		expect.NoErr(t, err)
		expect.DeepEqual(t, all, want)
	})

	t.Run("doesn't add artist if already added", func(t *testing.T) {
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{
				"boysnoize": {
					Artist:     ra.Artist{RAID: "943", Name: "Boys Noize"},
					EventsData: []ra.Event{},
				},
			},
			now,
		)

		want := Artists{
			{
				ID:            1,
				RAID:          "943",
				RASlug:        "boysnoize",
				Name:          "Boys Noize",
				FollowedBy:    UserIDs{UserID(1)},
				TrackedEvents: EventIDs{},
			},
		}
		err := registry.Follow(ctx, "boysnoize", UserID(1))

		expect.NoErr(t, err)
		all, err := registry.All(ctx)
		expect.NoErr(t, err)
		expect.DeepEqual(t, all, want)
	})

	t.Run("returns error if artist can't be found on RA", func(t *testing.T) {
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{},
			now,
		)

		err := registry.Follow(ctx, "unknown", UserID(1))

		expect.Err(t, err)
		expect.Equal(t, err.Error(), ErrNotFoundOnRA.Error())
	})

	t.Run("follows new artist as user", func(t *testing.T) {
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{
				"boysnoize": {
					Artist:     ra.Artist{RAID: "943", Name: "Boys Noize"},
					EventsData: []ra.Event{},
				},
				"sinamin": {
					Artist:     ra.Artist{RAID: "222", Name: "Sinamin"},
					EventsData: []ra.Event{},
				},
			},
			now,
		)

		err := registry.Follow(ctx, "boysnoize", UserID(1))
		expect.NoErr(t, err)

		err = registry.Follow(ctx, "sinamin", UserID(2))
		expect.NoErr(t, err)

		got, err := registry.ArtistsFor(ctx, UserID(1))
		want := Artists{
			Artist{
				ID:            1,
				RAID:          "943",
				RASlug:        "boysnoize",
				Name:          "Boys Noize",
				FollowedBy:    UserIDs{UserID(1)},
				TrackedEvents: EventIDs{},
			},
		}

		expect.NoErr(t, err)
		expect.DeepEqual(t, got, want)
	})

	t.Run("ignores follow if already following", func(t *testing.T) {
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{
				"boysnoize": {
					Artist:     ra.Artist{RAID: "943", Name: "Boys Noize"},
					EventsData: []ra.Event{},
				},
				"sinamin": {
					Artist:     ra.Artist{RAID: "222", Name: "Sinamin"},
					EventsData: []ra.Event{},
				},
			},
			now,
		)
		err := registry.Follow(ctx, "boysnoize", UserID(1))
		expect.NoErr(t, err)

		err = registry.Follow(ctx, "boysnoize", UserID(1))
		expect.NoErr(t, err)

		got, err := registry.ArtistsFor(ctx, UserID(1))

		expect.NoErr(t, err)
		expect.True(t, len(got) == 1)
		expect.DeepEqual(t, got[0].FollowedBy, UserIDs{UserID(1)})
	})

	t.Run("follows existing artist", func(t *testing.T) {
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{
				"boysnoize": {
					Artist:     ra.Artist{RAID: "943", Name: "Boys Noize"},
					EventsData: []ra.Event{},
				},
				"sinamin": {
					Artist:     ra.Artist{RAID: "222", Name: "Sinamin"},
					EventsData: []ra.Event{},
				},
			},
			now,
		)
		err := registry.Follow(ctx, "boysnoize", UserID(1))
		expect.NoErr(t, err)

		err = registry.Follow(ctx, "boysnoize", UserID(2))
		expect.NoErr(t, err)

		got, err := registry.ArtistsFor(ctx, UserID(2))

		expect.NoErr(t, err)
		expect.Equal(t, len(got), 1)
		expect.DeepEqual(t, got[0].FollowedBy, UserIDs{UserID(1), UserID(2)})
	})

	t.Run("fetches all events for artist in the next month", func(t *testing.T) {
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{
				"boysnoize": {
					Artist:     ra.Artist{RAID: "943", Name: "Boys Noize"},
					EventsData: []ra.Event{},
				},
				"sinamin": {
					Artist: ra.Artist{RAID: "222", Name: "Sinamin"},
					EventsData: []ra.Event{
						{
							Id:         "1",
							Title:      "Klubnacht",
							StartTime:  "2023-11-04T13:00:00.000",
							ContentUrl: "/events/1789025",
						},
						{
							Id:         "2",
							Title:      "Klubnacht 2",
							StartTime:  "2023-11-04T13:00:00.000",
							ContentUrl: "/events/1789025",
						},
					},
				},
			},
			now,
		)

		events, err := registry.EventsForArtist(ctx, Artist{
			ID:         1,
			RAID:       "222",
			RASlug:     "sinamin",
			Name:       "Sinamin",
			FollowedBy: nil,
		})

		expect.NoErr(t, err)
		expect.Equal(t, len(events), 2)
	})

	t.Run("reports only new events to user", func(t *testing.T) {
		events := ra.Events{
			{
				Id:         "3",
				Title:      "Kater Blau Night",
				StartTime:  "2023-11-04T13:00:00.000",
				ContentUrl: "/events/3",
				Venue: ra.Venue{
					Area: ra.Area{Name: "Berlin"},
					Name: "RSO",
				},
			},
			{
				Id:         "1",
				Title:      "Klubnacht",
				StartTime:  "2023-11-04T13:00:00.000",
				ContentUrl: "/events/1789025",
				Venue: ra.Venue{
					Area: ra.Area{Name: "Berlin"},
					Name: "RSO",
				},
			},
			{
				Id:         "2",
				Title:      "Klubnacht 2",
				StartTime:  "2023-11-04T13:00:00.000",
				ContentUrl: "/events/1789025",
				Venue: ra.Venue{
					Area: ra.Area{Name: "Berlin"},
					Name: "RSO",
				},
			},
		}
		registry, _ := NewInMemoryArtistRegistry(
			ra.ArtistStore{
				"boysnoize": {
					Artist:     ra.Artist{RAID: "943", Name: "Boys Noize"},
					EventsData: []ra.Event{events[0]},
				},
				"sinamin": {
					Artist:     ra.Artist{RAID: "222", Name: "Sinamin"},
					EventsData: []ra.Event{events[1], events[2]},
				},
			},
			now,
		)

		joe := UserID(1)

		err := registry.Follow(ctx, "boysnoize", joe)
		expect.NoErr(t, err)

		err = registry.Follow(ctx, "sinamin", joe)
		expect.NoErr(t, err)

		eventsForUser, err := registry.NewEventsForUser(ctx, joe)
		expect.NoErr(t, err)

		nov4th1pm := time.Date(2023, 11, 4, 13, 0, 0, 0, time.UTC)
		expect.DeepEqual(t, eventsForUser, Events{
			{
				Id:         EventID(3),
				Title:      "Kater Blau Night",
				Artist:     "Boys Noize",
				Venue:      "RSO",
				City:       "Berlin",
				StartTime:  nov4th1pm,
				ContentUrl: "/events/3",
			},
			{
				Id:         EventID(1),
				Title:      "Klubnacht",
				Artist:     "Sinamin",
				Venue:      "RSO",
				City:       "Berlin",
				StartTime:  nov4th1pm,
				ContentUrl: "/events/1789025",
			},
			{
				Id:         EventID(2),
				Title:      "Klubnacht 2",
				Artist:     "Sinamin",
				Venue:      "RSO",
				City:       "Berlin",
				StartTime:  nov4th1pm,
				ContentUrl: "/events/1789025",
			},
		},
		)

		newlyFetched, err := registry.NewEventsForUser(ctx, joe)

		expect.NoErr(t, err)
		expect.Len(t, newlyFetched, 0)
	})
}
