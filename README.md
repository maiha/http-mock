[![Build Status](https://travis-ci.org/maiha/http-mock.svg?branch=master)](https://travis-ci.org/maiha/http-mock)
[![scala 2.11](https://maven-badges.herokuapp.com/maven-central/sc.ala/http-mock_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/sc.ala/http-mock_2.11)
[![scala 2.12](https://maven-badges.herokuapp.com/maven-central/sc.ala/http-mock_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/sc.ala/http-mock_2.12)
[![scaladoc](http://javadoc-badge.appspot.com/sc.ala/http-mock_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/sc.ala/http-mock_2.11)

# HttpMock

Real http server for stubbing and expectations in Scala

Features
========

- Stubbing HTTP requests at real Http Server
  - responds always OK for any methods and paths
  - supported methods: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS
- Expecting HTTP requests as AccessLog
  - asserts AccessLog about methods and counts

Stubbing
========

#### start (random port)

```scala
import sc.ala.http.mock._
val server = HttpMock.start()
server.port  // => 37781 (automatically set by default)
// send requests to "http://127.0.0.1:37781" (or server.url)
server.stop
```

#### start with port

```scala
val server = HttpMock.start(9000)
server.port  // => 9000
```

#### restartable

- HttpMock is immutable

```scala
val server    = HttpMock.start(9000)  // GET: ok
val stopped   = server.stop()         // GET: error
val restarted = stopped.start()       // GET: ok
```

#### loan pattern

- `run()` ensures `server.stop()` after action

```scala
HttpMock.run()      { server => ... }
HttpMock.run(port)  { server => ... }
Setting(port).run() { server => ... }
```

#### restrict methods

- default: accept all methods

```scala
val server = Setting(methods = Set(GET, POST)).start()
/*
  GET  => 200
  POST => 200
  PUT  => 404
*/
```

### customize request handler

```scala
val server = Setting(handler = {
    case h: RequestHeader if h.version == "HTTP/1.0" => Results.HttpVersionNotSupported
})
/*
  GET  => 200
  GET with HTTP/1.0 => 505
*/
```

Expectations
============

- implemented expectations:
  - method, body, header, count

```scala
import sc.ala.http.mock._
import scala.concurrent.duration._

val server = HttpMock.start(9000)
```

#### assert methods and counts

```shell
curl http://127.0.0.1:9000/
```

```scala
server.logs.expect(GET , count = 1)(1.second)  // (PASS)
server.logs.expect(GET , count = 2)(1.second)  // java.lang.AssertionError
server.logs.expect(POST, count = 1)(1.second)  // java.lang.AssertionError

server.stop()
```

#### assert methods and body and headers

```shell
curl -X POST -H "Content-type: application/octet-stream" http://127.0.0.1:9000/ -d foo
curl -X POST -H "Content-type: application/octet-stream" http://127.0.0.1:9000/ -d bar
curl -X POST -H "Content-type: application/octet-stream" -H "X-ID: 1" http://127.0.0.1:9000/ -d bar
```

```scala
server.logs.expect(POST).body("foo")(1.second)           // (PASS)
server.logs.expect(POST).body("bar")(1.second)           // java.lang.AssertionError
server.logs.expect(POST).body("bar").count(2)(1.second)  // (PASS)
server.logs.expect(POST).body("baz")(1.second)           // java.lang.AssertionError
server.logs.expect(POST).header("X-ID", "1")(1.second)   // (PASS)

server.stop()
```

#### assert bodies as Set

```shell
curl -X POST -H "Content-type: application/octet-stream" http://127.0.0.1:9000/ -d foo
curl -X POST -H "Content-type: application/octet-stream" http://127.0.0.1:9000/ -d bar
curl -X POST -H "Content-type: application/octet-stream" -H "X-ID: 1" http://127.0.0.1:9000/ -d bar
```

```scala
server.logs.expect(POST).bodies(Set("foo", "bar"))(1.second)  // (PASS)
server.logs.expect(POST).bodies(Set("bar", "foo"))(1.second)  // (PASS)
server.logs.expect(POST).bodies(Set("foo", "XXX"))(1.second)  // java.lang.AssertionError

server.stop()
```

#### using in Spec

```scala
import sc.ala.http.mock._
import scala.concurrent.duration._
import org.scalatest.FunSpec

class FooSpec extends FunSpec {
  describe("foo") {
    it("test with real httpd") {
      HttpMock.run { server =>
        // your application logic to `server.url`
        ...

        // assert your requests like this
        server.logs.expect(POST, count = 2)(3.seconds)
      }
    }
  }
}
```


TODO
====

#### Expectations

- support path, request parameters and request bodies

Library
=======

See [build.sbt](./build.sbt)

Release
=======

```shell
% sbt
> + compile
> + publishSigned
> + sonatypeRelease
```

If you got `Unable to find credentials` in `publishSigned` phase,
it might have to do with credentials at `~/.sbt/0.13/sonatype.sbt`.

```
[error] Unable to find credentials for [Sonatype Nexus Repository Manager @ oss.sonatype.org].
[trace] Stack trace suppressed: run last *:publishSigned for the full output.
[error] (*:publishSigned) java.io.IOException: Access to URL https://oss.sonatype.org/service/local/staging/deploy/maven2/sc/ala/http-mock_2.11/0.3.3/http-mock_2.11-0.3.3-javadoc.jar was refused by the server: Unauthorized
```

See: http://www.scala-sbt.org/release/docs/Using-Sonatype.html

