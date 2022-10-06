# WebHook Notification

Ping a webhook with status updates from GoCD.

## Building the code base

To build the jar, run `gradle clean assemble`

## Tests

`gradle clean test`

## Release

Create a tag on GitHub and it'll kick of a GitHub action that will generate
a build of the jar with the same version
