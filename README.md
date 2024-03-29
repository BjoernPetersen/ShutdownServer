# ShutdownServer ![CircleCI](https://img.shields.io/circleci/project/github/BjoernPetersen/ShutdownServer.svg) ![GitHub](https://img.shields.io/github/license/BjoernPetersen/ShutdownServer.svg) ![GitHub (pre-)release](https://img.shields.io/github/release/BjoernPetersen/ShutdownServer/all.svg)

Server program providing HTTP endpoints to shut down and reboot the machine and more.

**Note:** this will probably be rewritten with a smaller scope, and in a statically compiled language. Until then, this project won't receive any updates. 

## Installation

The easiest way to install and use the program is to install it via [scoop](https://scoop.sh/):

```bash
scoop bucket add misc https://github.com/BjoernPetersen/scoop-misc-bucket
scoop install ShutdownServer
```

You'll need to set up autostart yourself.

For Windows users, the easiest way is to either to type `shell:common startup`
in the "Run" prompt (`Win+R`) and insert a shortcut there. For more fine grained control
the Task Scheduler is recommended.

You can also find builds on the releases page or download the latest development build from CircleCI:

- [distribution zip](https://felixgail.github.io/CircleCIArtifactProvider/index.html?vcs-type=github&user=BjoernPetersen&project=ShutdownServer&build=latest&branch=master&filter=successful&path=dist/ShutdownServer.zip&token=f52a99263b4356b276b1270f4eaa9a738aa25e6e)
- [shadowed jar](https://felixgail.github.io/CircleCIArtifactProvider/index.html?vcs-type=github&user=BjoernPetersen&project=ShutdownServer&build=latest&branch=master&filter=successful&path=dist/ShutdownServer-all.jar&token=f52a99263b4356b276b1270f4eaa9a738aa25e6e)

## Compilation

Compile using [Gradle](https://gradle.org/) and Java 1.8:

```bash
./gradlew build
```

## Execution

You may simply run the project with Gradle:

```bash
./gradlew run
```

or you can run the following command, which builds the project and creates execution
scripts at `build/install/ShutdownServer/bin`:

```bash
./gradlew installDist
```

## API

All operations require a `token` header, Base64-encoded with UTF-8.
The token is configured in the config.yml ([see example file](config.example.yml)).

By default, the server accepts a `POST` request on the `/shutdown` path
to trigger a shutdown/reboot.
You can optionally provide a `time` query parameter to set a custom delay in seconds.
If you don't provide the `time` parameter, the default delay from the config will be used.
To trigger a reboot instead of a shutdown, use the `reboot=true` query parameter.

Sending a `DELETE` request on the `/shutdown` endpoint will abort any scheduled shutdown/reboot.

### Custom shutdown command

The default shutdown commands are expected to work for Windows and Linux machines.
Should you encounter any problems, please create an issue in this GitHub repo.
Meanwhile, you can define custom shutdown/reboot/abort commands
as demonstrated [in the example file](shutdown.example.yml).

### Custom operations

You can define (nearly) arbitrary custom endpoints by
creating [definition files](custom.example.yml) in the `custom` directory.
The file's name will serve as the path to the defined endpoint.

Example: Operations defined in `custom/myendpoint.yml` will be available as `http://localhost/myendpoint`.

For each operation, a list of actions can be defined. They will be executed in order of definition
and each has access to the output of all previous actions ([see template values](#template-values)).

The output of the last action will be used as the HTTP response body.

A JSON Schema of the custom endpoint file format can be found
[in this repo](ShutdownServer-schema.json).

The following actions are available:

**Echo:**

key | type | description | required
----|------|-------------|---------
echo | `string` | Message to write | Yes
code | `integer` | HTTP status code to use | No; default: 200

**NoContent:**

Sends a `204 No Content` response.

key | type | description | required
----|------|-------------|---------
content | `boolean` | Must be false | Yes

**Command:**

Runs any program Java is capable of running as its own process.
The output of this action is the `STDOUT` output of the process.

key | type | description | required
----|------|-------------|---------
command | `array<string>` | The command and arguments you want to run | Yes
workingDir | `string` | The working directory for the program | No
detached | `boolean` | If true, the server won't wait for the program to terminate (implies code 202) | No; default: false
code | `integer` | HTTP status code to send **if the command exits with code 0** | No; default: 200
ignoreExitCode | `boolean` | Whether to ignore the command's exit code | No; default: false

You may also use the short version of this action by specifying a command string
as `cmd` instead of `command`. The command will be split by spaces.

**Powershell:**

Runs a command in PowerShell 6+. The command is invoked using the `pwsh` executable, which must be
in the PATH. The output of this action is the `STDOUT` output of the process.

key | type | description | required
----|------|-------------|---------
pwsh | `string` | The command you want to run | Yes
workingDir | `string` | The working directory for the program | No
detached | `boolean` | If true, the server won't wait for the program to terminate (implies code 202) | No; default: false
code | `integer` | HTTP status code to send **if the command exits with code 0** | No; default: 200
ignoreExitCode | `boolean` | Whether to ignore the command's exit code | No; default: false

You may also use the short version of this action by specifying a command string
as `cmd` instead of `command`. The command will be split by spaces.

#### Template values

You can use [string templates](https://www.stringtemplate.org/) anywhere in your actions.

The following values are available:

key | description | availability
----|-------------|-------------
`header.*` | All HTTP header parameters | Always
`query.*` | All HTTP query parameters | Always
`body.*` | The HTTP JSON body | `PUT` and `POST` requests
`output.[0-i]` | The output of the previous actions. Example: `output.1` for the output of the second action. | Always

## Configuration

You can configure the port, token and default shutdown time in a `config.yml` file.
An example file explaining the different options can be found [in the root directory of this repo](config.example.yml).
