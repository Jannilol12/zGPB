# zGPB
general purpose discord bot

## Prerequisites
* Java version >= 16 or 15 with `--enable-preview` flag set
* Discord API Token
* IDM credentials (if you want to use grade checking)

## Configuration
Via configuration file (`config.settings` or environment variables)

Note: *environment variables take priority over file settings*

## Running the code

Building from source:
```
gradle build
```

Bundling the jar (with dependencies):
```
gradle shadowJar
```


Execute the generated jar in build/libs afterwards, somewhat like this:
`java -jar build/libs/zGPB-[CURRENT_VERSION]-all.jar [--enable-preview]`

`CURRENT_VERSION` refers to 1.2, 1.3, ...
