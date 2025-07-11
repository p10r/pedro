package internal

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

func (artists ArtistEntities) Put(entity ArtistEntity) ArtistEntities {
	var soundcloudUrns []string
	for _, artist := range artists {
		soundcloudUrns = append(soundcloudUrns, artist.SoundcloudUrn)
	}
	if !slices.Contains(soundcloudUrns, entity.SoundcloudUrn) {
		artists = append(artists, entity)
	}
	return artists
}

func (artists ArtistEntities) Remove(name string) (entity ArtistEntities, found bool) {
	for i, e := range artists {
		if e.Name == name {
			artists[i] = artists[len(artists)-1]
			return artists[:len(artists)-1], true
		}
	}
	return artists, false
}

func (artists ArtistEntities) Names() []string {
	var names []string
	for _, entity := range artists {
		names = append(names, entity.Name)
	}
	if len(names) == 0 {
		return []string{}
	}
	return names
}

type JsonDb struct {
	db *jsonfile.JSONFile[UserEntities]
}

func NewJsonDb(path string) (*JsonDb, error) {
	db, err := jsonfile.New[UserEntities](path)
	if err != nil {
		return nil, fmt.Errorf("json db: failed creating repository: %w", err)
	}
	return &JsonDb{db}, err
}

func (r *JsonDb) Save(user UserEntity) error {
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
		return fmt.Errorf("json db: failed writing to json file: %w", err)
	}

	return nil
}

func (r *JsonDb) All() UserEntities {
	var users UserEntities
	r.db.Read(func(db *UserEntities) {
		users = *db
	})
	return users
}

func (r *JsonDb) Get(userId int64) (UserEntity, bool) {
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
