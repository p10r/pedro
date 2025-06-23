package soundcloud

import (
	"encoding/json"
	"fmt"
	"github.com/p10r/monolith/httputil"
	"net/http"
	"strings"
)

type client struct {
	authUrl      string
	base64Secret string
	client       *http.Client
}

func newClient(url string, base64Secret string) *client {
	return &client{authUrl: strings.TrimSuffix(url, "/"), base64Secret: base64Secret, client: httputil.NewDefaultClient()}
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
	url := c.authUrl + "/oauth/token"
	req, err := http.NewRequest("POST", url, data)
	if err != nil {
		return authRes{}, fmt.Errorf("soundcloud api: unexpected error: %w", err)
	}

	req.Header.Set("accept", "application/json; charset=utf-8")
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Set("Authorization", "Basic "+c.base64Secret)

	resp, err := c.client.Do(req)
	if err != nil {
		return authRes{}, fmt.Errorf("soundcloud api: error executing req: %w, url: %v", err, url)
	}
	defer resp.Body.Close()

	if resp.StatusCode == 401 {
		return authRes{}, newUnauthorizedError("POST", url)
	}

	if resp.StatusCode != 200 {
		return authRes{}, fmt.Errorf("soundcloud api: got %v when authorizing, url: %v", resp.Status, url)
	}

	var authResponse authRes
	err = json.NewDecoder(resp.Body).Decode(&authResponse)
	if err != nil {
		return authRes{}, fmt.Errorf("soundcloud api: error when parsing json: %w, %v", err, url)
	}

	return authResponse, nil
}
