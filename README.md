# Pedro

## Package

```
./gradlew jib
```

More details on building JIB can be found
at [https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin]()

## Deployment Setup

This project can basically be deployed anywhere since it's being distributed as Docker
container.

Here's an example for fly.io, which I use to deploy the App:

1. [Create an empty app](https://fly.io/docs/languages-and-frameworks/dockerfile/)
2. [Create a Fly volume](https://fly.io/docs/apps/volume-storage/#configure-the-app-to-mount-the-new-volume-2)
3. [Set up the database URL as secret](https://fly.io/docs/rails/advanced-guides/sqlite3/): `DATABASE_URL`
   


