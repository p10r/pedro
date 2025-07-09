package db

import (
	"crawshaw.dev/jsonfile"
	"fmt"
	"slices"
)

type UserEntities struct {
	Users []UserEntity `json:"users"`
}

type UserEntity struct {
	TelegramId int64          `json:"telegramId"`
	Artists    ArtistEntities `json:"artists"`
}

type ArtistEntity struct {
	Name          string `json:"name"`
	SoundcloudUrl string `json:"soundcloudUrl"`
	SoundcloudUrn string `json:"soundcloudUrn"`
}
type ArtistEntities []ArtistEntity

func (a ArtistEntities) Put(entity ArtistEntity) ArtistEntities {
	var soundcloudUrns []string
	for _, artist := range a {
		soundcloudUrns = append(soundcloudUrns, artist.SoundcloudUrn)
	}
	if !slices.Contains(soundcloudUrns, entity.SoundcloudUrn) {
		a = append(a, entity)
	}
	return a
}

type JsonRepository struct {
	db *jsonfile.JSONFile[UserEntities]
}

func NewJsonRepository(path string) (*JsonRepository, error) {
	db, err := jsonfile.New[UserEntities](path)
	if err != nil {
		return nil, fmt.Errorf("json repo: failed creating repository: %w", err)
	}
	return &JsonRepository{db}, err
}

func (r *JsonRepository) Save(user UserEntity) error {
	if user.Artists == nil {
		user.Artists = []ArtistEntity{}
	}

	err := r.db.Write(func(dbUsers *UserEntities) error {
		if dbUsers.Users == nil {
			dbUsers.Users = []UserEntity{}
		}

		for i, dbUser := range dbUsers.Users {
			if dbUser.TelegramId == user.TelegramId {
				dbUsers.Users[i].TelegramId = user.TelegramId
				dbUsers.Users[i].Artists = user.Artists
				return nil
			}
		}

		dbUsers.Users = append(dbUsers.Users, user)
		return nil
	})
	if err != nil {
		return fmt.Errorf("json repo: failed writing to json file: %w", err)
	}

	return nil
}

func (r *JsonRepository) All() UserEntities {
	var users UserEntities
	r.db.Read(func(db *UserEntities) {
		users = *db
	})
	return users
}

func (r *JsonRepository) Get(userId int64) (UserEntity, bool) {
	var user *UserEntity
	var userFound bool
	r.db.Read(func(dbUsers *UserEntities) {
		for _, e := range dbUsers.Users {
			if e.TelegramId == userId {
				user = &e
				userFound = true
				return
			}
		}
	})

	if !userFound {
		return UserEntity{}, false
	}

	if user == nil {
		return UserEntity{}, false
	}

	return *user, true
}
