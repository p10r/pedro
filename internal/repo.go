package internal

import "context"

type Artist struct {
	ID int64
}

type Repository interface {
	Add(ctx context.Context, artist Artist) error
	ListAllFor(ctx context.Context, id UserId) ([]Artist, error)
}
