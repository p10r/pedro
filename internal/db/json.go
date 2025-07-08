package db

import (
	"crawshaw.dev/jsonfile"
	"fmt"
)

type UserEntities struct {
	Users []UserEntity `json:"users"`
}

func (u UserEntities) Get(userId int64) (entity UserEntity, found bool) {
	for _, entity := range u.Users {
		if entity.TelegramId == userId {
			return entity, true
		}
	}
	return UserEntity{}, false
}

func (u UserEntities) Save(user UserEntity) UserEntities {
	existing := u.Users
	for i, entity := range existing {
		if entity.TelegramId == user.TelegramId {
			existing[i] = user
			return UserEntities{existing}
		}
	}
	return UserEntities{append(u.Users, user)}
}

type UserEntity struct {
	TelegramId int64          `json:"telegramId"`
	Artists    []ArtistEntity `json:"artists"`
}

type ArtistEntity struct {
	Name          string `json:"name"`
	SoundcloudUrl string `json:"soundcloudUrl"`
	SoundcloudUrn string `json:"soundcloudUrn"`
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
	err := r.db.Write(func(db *UserEntities) error {
		updated := db.Save(user)
		db.Users = updated.Users
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

func (r *JsonRepository) Get(userId int64) UserEntity {
	var user UserEntity
	r.db.Read(func(db *UserEntities) {
		entity, found := db.Get(userId)
		if !found {
			user = UserEntity{}
		}
		user = entity
	})
	return user
}
