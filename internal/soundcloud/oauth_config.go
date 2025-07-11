package soundcloud

import "fmt"

// OAuthConfig provides all the necessary parameters for clientcredentials.Config
type OAuthConfig struct {
	clientId     string
	clientSecret string
	tokenUrl     string
}

func NewOAuthConfig(clientId, clientSecret, tokenUrl string) (OAuthConfig, error) {
	if clientId == "" {
		return OAuthConfig{}, fmt.Errorf("clientId can't be empty")
	}
	if clientSecret == "" {
		return OAuthConfig{}, fmt.Errorf("clientSecret can't be empty")
	}
	if tokenUrl == "" {
		return OAuthConfig{}, fmt.Errorf("tokenUrl can't be empty")
	}

	return OAuthConfig{
		clientId:     clientId,
		clientSecret: clientSecret,
		tokenUrl:     tokenUrl,
	}, nil
}
