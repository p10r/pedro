package internal

import (
	"context"
	"fmt"
	"github.com/p10r/pedro/internal/db"
	"log"
)

type UserId int64

type FollowArtistCmd struct {
	SoundcloudUrl string
	UserId        UserId
}

type Service struct {
	repo       *db.JsonRepository
	soundcloud Soundcloud
}

type Soundcloud interface {
	ArtistByUrl(url string) (SoundcloudArtist, error)
}

type SoundcloudArtist struct {
	Id       int    `json:"id"`
	Urn      string `json:"urn"`
	Url      string `json:"permalink_url"`
	Username string `json:"username"`
}

func NewService(repo *db.JsonRepository, soundcloudClient Soundcloud) *Service {
	return &Service{repo: repo, soundcloud: soundcloudClient}
}

func (s *Service) FollowArtist(ctx context.Context, cmd FollowArtistCmd) (string, error) {
	id := int64(cmd.UserId)
	user, found := s.repo.Get(id)

	// There's no explicit endpoint to add users, since access toggled via an env variable.
	// This means that for now, every user who has API access will be created transparently in the DB, if missing.
	if !found {
		log.Printf("No user with id %v, creating a new entry", id)
		err := s.createUser(id)
		if err != nil {
			return "", fmt.Errorf("got err when trying to create user: %w", err)
		}
		return s.FollowArtist(ctx, cmd)
	}

	scArtist, err := s.soundcloud.ArtistByUrl(cmd.SoundcloudUrl)
	if err != nil {
		return "", fmt.Errorf("error when trying to find artist %s: %w", cmd.SoundcloudUrl, err)
	}

	user.Artists = user.Artists.Put(db.ArtistEntity{
		Name:          scArtist.Username,
		SoundcloudUrl: scArtist.Url,
		SoundcloudUrn: scArtist.Urn,
	})

	err = s.repo.Save(user)
	if err != nil {
		return "", fmt.Errorf("err when trying to follow artist %v. err: %w", cmd.SoundcloudUrl, err)
	}

	return scArtist.Username, nil
}

func (s *Service) createUser(id int64) error {
	err := s.repo.Save(db.UserEntity{
		TelegramId: id,
	})
	if err != nil {
		return fmt.Errorf("could not create user %v, err: %w", id, err)
	}
	return nil
}

type Artists []Artist

type Artist struct {
	Name string
	Url  string
}

func (s *Service) ListArtists(_ context.Context, userId UserId) (Artists, error) {
	id := int64(userId)
	userEntity, found := s.repo.Get(id)
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
