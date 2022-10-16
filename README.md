# Purpose
Define a idiomatic struct of `dir` about the beckend App of ZIO.

# Domain
- zio, function program
- zio-quill, database access
- zio-http, restful server or whatever
- zio-json, encode/decode

# Currently idea
```
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

## Any idea?
Please share with us, for free.

# Notice
`zio-http` is not relase with zio for this writing. You can get some help from [here](https://github.com/zio/zio-http/issues/1532).