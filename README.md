# Purpose
Define a idiomatic struct of `dir` about the beckend App of ZIO.

# Domain
- zio, function program
- zio-quill, database access
- zio-http, restful server or whatever
- zio-json, encode/decode

# Currently idea
```
src/resources
  - application.conf / .properties
src/main/scala/your-project
  - db
    - model
      - User.scala               // case class, for model
      - xx.scala
    - table
      - Users.scala              // crud prue access, with quill
      - xxs.scala
    - QuillContext.scala         // Quill Context
  - http
    - UserPartial.scala          // define routes, partially
    - xxPartial.scala
  - service
    - business
      - Notifications.scala      // for app use case
    - common
      - EmailService.scala       // some common use case
  - MainApp.scala                // entry point
```

## Http vs ParticalFunction
There are two different way to define routes:

- Http ++ Http, eg. `EventRoute`
- ParticalFunction#orElse, like `UserPartial` and `RsvpPartial`

## Quill compile log
```
sbt -Dquill.macro.log.pretty=true

sbt -Dquill.macro.log=false
```

## Config 
> https://github.com/lightbend/config

Loads the following (first-listed are higher priority)
- system properties
- application.conf (all resources on classpath with this name)
- application.json (all resources on classpath with this name)
- application.properties (all resources on classpath with this name)
- reference.conf (all resources on classpath with this name)

## Build application packages
> https://github.com/sbt/sbt-native-packager

```
sbt universal:packageBin
```

## Any idea?
Please share with us, for free.

# Notice
`zio-http` is not relase with zio for this writing. You can get some help from [here](https://github.com/zio/zio-http/issues/1532).