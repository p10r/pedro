package internal

type FollowArtistCmd struct {
	SoundcloudUrl string
	UserId        UserId
}

type UnfollowArtistCmd struct {
	ArtistName string
	UserId     UserId
}
