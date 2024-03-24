package telegram

import (
	"gopkg.in/telebot.v3"
	"gopkg.in/telebot.v3/middleware"
	"log"
	"pedro-go/db"
	"pedro-go/domain"
	"pedro-go/ra"
	"time"
)

func Pedro(botToken, dsn string, allowedUserIds []int64) {
	now := func() time.Time { return time.Now() }

	conn := db.NewDB(dsn)
	err := conn.Open()
	if err != nil {
		log.Fatal(err)
	}
	log.Printf("DSN is set to %v", dsn)

	repo := db.NewSqliteArtistRepository(conn)

	m := db.NewEventMonitor(conn)
	artistRegistry := domain.NewArtistRegistry(repo, ra.NewClient("https://ra.co"), m, now)

	bot, err := telebot.NewBot(
		telebot.Settings{
			Token:   botToken,
			Poller:  &telebot.LongPoller{Timeout: 10 * time.Second},
			Verbose: false,
		},
	)
	if err != nil {
		log.Fatal(err)
		return
	}

	bot.Use(middleware.Whitelist(allowedUserIds...))

	log.Print("Started Pedro")

	n := &Notifier{
		bot:      bot,
		registry: artistRegistry,
		users:    allowedUserIds,
	}

	go n.StartEventNotifier()

	sender := TelebotSender{}

	//bot.Use(middleware.Logger())
	bot.Handle("/follow", followArtist(artistRegistry, sender))
	bot.Handle("/artists", listArtists(artistRegistry, sender))
	bot.Handle("/events", listEvents(artistRegistry, sender))
	bot.Start()
}
