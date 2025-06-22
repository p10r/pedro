## Local Development

Install [.direnv](https://github.com/direnv/direnv), [Lefthook](https://github.com/evilmartians/lefthook) & [nilaway](https://github.com/uber-go/nilaway?tab=readme-ov-file#standalone-checker)

```bash
lefthook install

cp .envrc-example .envrc

direnv allow .

go run cmd/main.go
```