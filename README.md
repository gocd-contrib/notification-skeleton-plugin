# WebHook Notification

Ping a webhook with status updates from GoCD.

## Building the code base

To build the jar, run `gradle clean assemble`

## Tests

`gradle clean test`

## Release

Create a tag and it'll kick off a GitHub action to generate a release
with a jar file for the plugin.

```shell
git tag v0.0.1
git push origin --tags
```
