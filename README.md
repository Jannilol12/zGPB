# zGPB
general purpose discord bot

## Prerequisites
* Java version >= 16 or 15 with `--enable-preview` flag set
* Discord API Token
* IDM credentials

## Configuration
Via configuration file (`config.settings` or environment variables)

Note: *environment variables will be used with higher priority than settings in the file*

## Running the code

Building from source:
```
gradle jar
```
Execute the generate jar in build/libs afterwards, somewhat like this:
`java -jar build/libs/zGPB-[CURRENT_VERSION].jar [--enable-preview]`

`CURRENT_VERSION` refers to 1.2, 1.3, ...
