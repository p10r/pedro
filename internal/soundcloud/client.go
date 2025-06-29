package soundcloud

import (
	"encoding/json"
	"fmt"
	"github.com/p10r/pedro/httputil"
	"net/http"
	url2 "net/url"
	"strings"
)

type client struct {
	authUrl      *url2.URL
	apiUrl       *url2.URL
	base64Secret string
	client       *http.Client
}

func newClient(authUrl, apiUrl string, base64Secret string) (*client, error) {
	authBase, err := url2.Parse(strings.TrimSuffix(authUrl, "/"))
	if err != nil {
		return nil, fmt.Errorf("soundcloud api: cannot parse authUrl %v, err: %w", authUrl, err)
	}

	apiBase, err := url2.Parse(strings.TrimSuffix(apiUrl, "/"))
	if err != nil {
		return nil, fmt.Errorf("soundcloud api: cannot parse apiUrl %v, err: %w", apiUrl, err)
	}

	return &client{
		authUrl:      authBase,
		apiUrl:       apiBase,
		base64Secret: base64Secret,
		client:       httputil.NewDefaultClient(),
	}, nil
}

type unauthorizedErr struct {
	method string
	url    string
}

func newUnauthorizedError(method, url string) *unauthorizedErr {
	return &unauthorizedErr{method: method, url: url}
}

func (*unauthorizedErr) Error() string {
	return "Unauthorized"
}

type authRes struct {
	AccessToken  string `json:"access_token"`
	TokenType    string `json:"token_type"`
	ExpiresIn    int    `json:"expires_in"`
	RefreshToken string `json:"refresh_token"`
	Scope        string `json:"scope"`
}

func (c *client) Authorize() (authRes, error) {
	var data = strings.NewReader(`grant_type=client_credentials`)
	c.authUrl.Path += "/oauth/token"

	authUrl := c.authUrl.String()
	req, err := http.NewRequest("POST", authUrl, data)
	if err != nil {
		return authRes{}, fmt.Errorf("soundcloud api: unexpected error: %w", err)
	}

	req.Header.Set("accept", "application/json; charset=utf-8")
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Set("Authorization", "Basic "+c.base64Secret)

	res, err := c.client.Do(req)
	if err != nil {
		return authRes{}, fmt.Errorf("soundcloud api: error executing req: %w, url: %v", err, authUrl)
	}
	//nolint:errcheck
	defer res.Body.Close()

	if res.StatusCode == 401 {
		return authRes{}, newUnauthorizedError("POST", authUrl)
	}

	if res.StatusCode != 200 {
		return authRes{}, fmt.Errorf("soundcloud api: got %v when authorizing, url: %v", res.Status, authUrl)
	}

	var authResponse authRes
	err = json.NewDecoder(res.Body).Decode(&authResponse)
	if err != nil {
		return authRes{}, fmt.Errorf("soundcloud api: error when parsing json: %w, %v", err, authUrl)
	}

	return authResponse, nil
}

func (c *client) ArtistByUrl(url string, oAuthToken string) (string, error) {
	params := url2.Values{}
	params.Add("url", url)
	c.apiUrl.RawQuery = params.Encode()

	req, err := http.NewRequest("GET", c.apiUrl.String(), nil)
	if err != nil {
		return "", fmt.Errorf("soundcloud api: unexpected error: %w", err)
	}

	req.Header.Set("accept", "application/json; charset=utf-8")
	req.Header.Set("Authorization", "OAuth "+oAuthToken)

	panic("")
}
