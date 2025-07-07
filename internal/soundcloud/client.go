package soundcloud

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/p10r/pedro/httputil"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/clientcredentials"
	"net/http"
	url2 "net/url"
	"strings"
)

type client struct {
	apiUrl *url2.URL
	client *http.Client
}

func newClient(conf oAuthConfig, apiUrl string) (*client, error) {
	ctx := context.Background()
	ctx = context.WithValue(ctx, oauth2.HTTPClient, httputil.NewDefaultClient())

	config := &clientcredentials.Config{
		ClientID:       conf.clientId,
		ClientSecret:   conf.clientSecret,
		TokenURL:       conf.tokenUrl,
		Scopes:         nil,
		EndpointParams: nil,
		AuthStyle:      oauth2.AuthStyleAutoDetect,
	}

	authedClient := config.Client(ctx)

	baseUrl, err := url2.Parse(strings.TrimSuffix(apiUrl, "/"))
	if err != nil {
		return nil, fmt.Errorf("soundcloud.client: cannot parse apiUrl %v, err: %w", apiUrl, err)
	}

	return &client{
		apiUrl: baseUrl,
		client: authedClient,
	}, nil
}

type Artist struct {
	Id       int    `json:"id"`
	Urn      string `json:"urn"`
	Url      string `json:"permalink_url"`
	Username string `json:"username"`
}

func (c *client) ArtistByUrl(url string) (Artist, error) {
	params := url2.Values{}
	params.Add("url", url)

	apiUrl, err := url2.Parse("https://api.soundcloud.com/resolve")
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: Cannot parse: %w", err)
	}
	apiUrl.RawQuery = params.Encode()

	req, err := http.NewRequest("GET", apiUrl.String(), nil)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: unexpected error: %w", err)
	}

	req.Header.Set("accept", "application/json; charset=utf-8")

	res, err := c.client.Do(req)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: error executing req: %w", err)
	}
	//nolint:errcheck
	defer res.Body.Close()

	if res.StatusCode != 200 {
		return Artist{}, fmt.Errorf("status code is %v", res.Status)
	}

	var artist Artist
	err = json.NewDecoder(res.Body).Decode(&artist)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: error when parsing json: %w", err)
	}

	return artist, nil
}

func (c *client) ArtistByUrn(urn string) (Artist, error) {
	fullPath, err := url2.JoinPath("https://api.soundcloud.com/users/", url2.PathEscape(urn))
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: cannot parse urn %v: %w", fullPath, err)
	}

	apiUrl, err := url2.Parse(fullPath)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: cannot parse url %v: %w", fullPath, err)
	}

	req, err := http.NewRequest("GET", apiUrl.String(), nil)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: unexpected error: %w", err)
	}

	req.Header.Set("accept", "application/json; charset=utf-8")

	res, err := c.client.Do(req)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: error executing req: %w", err)
	}
	//nolint:errcheck
	defer res.Body.Close()

	if res.StatusCode != 200 {
		return Artist{}, fmt.Errorf("status code is %v", res.Status)
	}

	var artist Artist
	err = json.NewDecoder(res.Body).Decode(&artist)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.client: error when parsing json: %w", err)
	}

	return artist, nil
}
