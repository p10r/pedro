package internal

import (
	"context"
	"fmt"
	"slices"
	"strings"
)

type UserId int64

type service struct {
	db         *JsonDb
	soundcloud Soundcloud
}

func newService(db *JsonDb, soundcloudClient Soundcloud) *service {
	return &service{db: db, soundcloud: soundcloudClient}
}

func (service *service) FollowArtist(_ context.Context, cmd FollowArtistCmd) (string, error) {
	id := int64(cmd.UserId)
	user, found := service.db.Get(id)

	if !found {
		return "", fmt.Errorf("no user with id %v", id)
	}

	scArtist, err := service.findArtist(cmd)
	if err != nil {
		return "", err
	}

	user.Artists = user.Artists.Put(ArtistEntity{
		Name:          scArtist.Username,
		SoundcloudUrl: scArtist.Url,
		SoundcloudUrn: scArtist.Urn,
	})

	err = service.db.Save(user)
	if err != nil {
		return "", fmt.Errorf("err when trying to follow artist %v. err: %w", cmd.SoundcloudUrl, err)
	}

	return scArtist.Username, nil
}

func (service *service) findArtist(cmd FollowArtistCmd) (SoundcloudArtist, error) {
	if cmd.SoundcloudUrl != "" {
		artist, err := service.soundcloud.ArtistByUrl(cmd.SoundcloudUrl)
		if err != nil {
			return SoundcloudArtist{}, fmt.Errorf("error when trying to find artist %s: %w", cmd.SoundcloudUrl, err)
		}
		return artist, nil
	}

	if cmd.SoundcloudName != "" {
		artists, err := service.soundcloud.ArtistByQuery(cmd.SoundcloudName)
		if err != nil {
			return SoundcloudArtist{}, fmt.Errorf("error when trying to find artist %s: %w", cmd.SoundcloudUrl, err)
		}
		if len(artists) == 0 {
			return SoundcloudArtist{}, fmt.Errorf("no artist found with name %s", cmd.SoundcloudName)
		}
		if len(artists) == 1 {
			return artists[0], nil
		}
		// Soundcloud doesn't always return the best match, e.g., when searching for Anna Reusch.
		// Here, we prioritize the follower count, expecting that this will provide better results to users.
		artist := slices.MaxFunc(artists, func(a, b SoundcloudArtist) int {
			return a.FollowersCount - b.FollowersCount
		})
		return artist, nil
	}

	return SoundcloudArtist{}, fmt.Errorf("neither soundcloud name nor url were provided")
}

func (service *service) UnfollowArtist(_ context.Context, cmd UnfollowArtistCmd) (artistName string, err error) {
	userId := int64(cmd.UserId)
	user, found := service.db.Get(userId)
	if !found {
		return "", fmt.Errorf("no user with id %v", userId)
	}

	updatedArtists, found := user.Artists.Remove(cmd.ArtistName)
	if !found {
		err := fmt.Errorf("could not find artist %service in user'service artists: %service", cmd.ArtistName, strings.Join(user.Artists.Names(), ", "))
		return "", err
	}

	user.Artists = updatedArtists
	err = service.db.Save(user)
	if err != nil {
		return "", fmt.Errorf("err when trying to unfollow artist %v. err: %w", cmd.ArtistName, err)
	}

	return cmd.ArtistName, nil
}

type Artists []Artist

type Artist struct {
	Name string
	Url  string
}

func (service *service) ListArtists(_ context.Context, cmd ListArtistsCmd) (Artists, error) {
	id := int64(cmd.UserId)
	userEntity, found := service.db.Get(id)
	if !found {
		return Artists{}, fmt.Errorf("no user with id %v", id)
	}

	var artists Artists
	for _, artist := range userEntity.Artists {
		artists = append(artists, Artist{Name: artist.Name, Url: artist.SoundcloudUrl})
	}

	if len(artists) == 0 {
		return Artists{}, nil
	}

	return artists, nil
}

func (service *service) CreateUser(id int64) error {
	err := service.db.Save(UserEntity{
		TelegramId: id,
	})
	if err != nil {
		return fmt.Errorf("could not create user %v, err: %w", id, err)
	}
	return nil
}
