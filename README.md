## Local Development

- install [.direnv](https://github.com/direnv/direnv)
- install [Lefthook](https://github.com/evilmartians/lefthook)
- install [nilaway](https://github.com/uber-go/nilaway?tab=readme-ov-file#standalone-checker)
- run `lefthook install` to set up pre-commit hooks
- copy `.envrc-example` to `.envrc`, adjusting the values
- run `direnv allow .`
- run `go run cmd/main.go`
- a new `local.db` will be created in `local/`

## Deployment

```
docker build -t pedro .
# to inspect:
docker run -it pedro

docker run pedro

fly launch
fly volume create pedrovolume -r ams -n 1
fly deploy
fly logs
```

## Connecting to Prod DB

Since sqlite is file based, we can simply download the db.
For that to work, follow this
thread: [fly.io forum](https://community.fly.io/t/scp-a-file-into-a-persistent-volume/2729)
[Here's](https://www.richardneililagan.com/posts/copying-files-to-fly-io-volume/) another
article.

```
cd prod/
scp root@<app name in fly.toml>.internal:/data/pedro.db .
scp root@<app name in fly.toml>.internal:/data/pedro.db-shm .
scp root@<app name in fly.toml>.internal:/data/pedro.db-wal .
```

## TODOs

- [move away from Docker](https://fly.io/docs/languages-and-frameworks/golang/)