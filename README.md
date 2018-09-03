# ShutdownServer ![CircleCI](https://img.shields.io/circleci/project/github/BjoernPetersen/ShutdownServer.svg) ![GitHub](https://img.shields.io/github/license/BjoernPetersen/ShutdownServer.svg) ![GitHub (pre-)release](https://img.shields.io/github/release/BjoernPetersen/ShutdownServer/all.svg)

HTTP server for Windows providing a `/shutdown` endpoint to shut down the machine.

## Installation
The easiest way to install and use the program is to install it via [scoop](https://scoop.sh/):
```
scoop bucket add misc https://github.com/BjoernPetersen/scoop-misc-bucket
scoop install ShutdownServer
```

You'll need to setup autostart yourself. The easiest way is to type `shell:common startup`
in the search or the "Run" prompt (`Win+R`) and insert a shortcut there.

## Compilation
Compile using [Gradle](https://gradle.org/) and Java 1.8:
```
./gradlew build
```

## Execution
You may simply run the project with Gradle:
```
./gradlew run
```

or you can run the following command, which builds the project and creates execution 
scripts at `build/install/ShutdownServer/bin`:
```
./gradlew installDist
```

## API
The server accepts a POST request on the `/shutdown` path with a `token` header.

## Configuration
You can configure the port, token and shutdown time in a `config/.env` file.
An example file can be found [here](src/main/resources/config/.env.example).

Key | Description 
---- | ----
TOKEN | any random token to authenticate requests 
TIME | the time in seconds between successful request and shutdown
PORT | the port to listen on
