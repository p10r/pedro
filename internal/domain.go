package internal

import (
	"context"
	"fmt"
	"log"
	"strings"
)

type UserId int64

type Service struct {
	db         *JsonDb
	soundcloud Soundcloud
}

func NewService(db *JsonDb, soundcloudClient Soundcloud) *Service {
	return &Service{db: db, soundcloud: soundcloudClient}
}

func (service *Service) FollowArtist(ctx context.Context, cmd FollowArtistCmd) (string, error) {
	id := int64(cmd.UserId)
	user, found := service.db.Get(id)

	// There's no explicit endpoint to add users, since access toggled via an env variable.
	// This means that for now, every user who has API access will be created transparently in the DB, if missing.
	if !found {
		log.Printf("No user with id %v, creating a new entry", id)
		err := service.createUser(id)
		if err != nil {
			return "", fmt.Errorf("got err when trying to create user: %w", err)
		}
		return service.FollowArtist(ctx, cmd)
	}

	scArtist, err := service.soundcloud.ArtistByUrl(cmd.SoundcloudUrl)
	if err != nil {
		return "", fmt.Errorf("error when trying to find artist %service: %w", cmd.SoundcloudUrl, err)
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

func (service *Service) UnfollowArtist(_ context.Context, cmd UnfollowArtistCmd) (artistName string, err error) {
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

func (service *Service) ListArtists(_ context.Context, userId UserId) (Artists, error) {
	id := int64(userId)
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

func (service *Service) createUser(id int64) error {
	err := service.db.Save(UserEntity{
		TelegramId: id,
	})
	if err != nil {
		return fmt.Errorf("could not create user %v, err: %w", id, err)
	}
	return nil
}
