package ra

import (
	"errors"
	"fmt"
	"github.com/p10r/pedro/pedro/domain"
	"log/slog"
	"net"
	"net/http"
	"time"
)

var ErrSlugNotFound = errors.New("slug not found on ra.co")

type Client struct {
	http    *http.Client
	baseUri string
	log     *slog.Logger
}

func NewClient(baseUri string, log *slog.Logger) *Client {
	c := &http.Client{
		Timeout: 10 * time.Second,
		Transport: &http.Transport{
			DialContext: (&net.Dialer{
				Timeout:   10 * time.Second,
				KeepAlive: 10 * time.Second,
			}).DialContext,
			TLSHandshakeTimeout:   5 * time.Second,
			ResponseHeaderTimeout: 5 * time.Second,
			ExpectContinueTimeout: 1 * time.Second,
		},
	}

	return &Client{http: c, baseUri: baseUri, log: log}
}

func (c *Client) GetArtistBySlug(slug domain.RASlug) (domain.ArtistInfo, error) {
	req, err := newGetArtistReq(slug, c.baseUri)
	if err != nil {
		return domain.ArtistInfo{}, fmt.Errorf("could not create request: %w", err)
	}

	res, err := c.http.Do(req)
	if err != nil {
		return domain.ArtistInfo{}, fmt.Errorf("could not get response: %w", err)
	}

	a, err := NewArtist(res)
	if err != nil {
		return domain.ArtistInfo{}, err
	}
	return a.ToArtistInfo(), nil
}

func (c *Client) GetEventsByArtistId(
	a domain.Artist,
	start time.Time,
	end time.Time,
) (domain.Events, error) {
	req, err := newGetEventsReq(a.RAID, start, end, c.baseUri)
	if err != nil {
		return domain.Events{}, fmt.Errorf("could not create request: %w", err)
	}

	res, err := c.http.Do(req)
	e, err := NewEvent(res, err, c.log)
	if err != nil {
		return domain.Events{}, fmt.Errorf("could not get response: %v", err)
	}
	return e.ToDomainEvents(a.Name, c.log), err
}
