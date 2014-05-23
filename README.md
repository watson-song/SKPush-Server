[![Build Status](https://travis-ci.org/vngrs/tcp-async.png?branch=master)](https://travis-ci.org/vngrs/tcp-async)
[![Coverage Status](https://coveralls.io/repos/vngrs/tcp-async/badge.png?branch=master)](https://coveralls.io/r/vngrs/tcp-async?branch=master)


tcp-async
===========

What
-----
<i>tcp-async</i> is an typesafe activator template which handles tcp requests reactively.


Why
----
When you reached to limits of your synchronous application, you will start to look for
an alternative way to expand your request handling capacity. That's where reactive programming will shine.

<a href="http://en.wikipedia.org/wiki/C10k_problem">Handling 10K Requests</a> is not a trivial goal.
And reaching to that numbers is may cost a lot with standart programming architectures.

<i>tcp-async</i> shows how to implement a simple and functional reactive application in <a href="http://www.scala-lang.org/">scala</a>.


How
---
By using <a href="http://akka.io/">Akka's</a> actor structure.


With
-----
* <a href="http://spray.io/documentation/1.2-M8/spray-client/">spray-client</a>
* <a href="http://spray.io/documentation/1.2-M8/spray-can/">spray-can</a>
* <a href="https://github.com/mauricio/postgresql-async">postgresql/mysql-async</a>
* <a href="http://doc.akka.io/docs/akka/2.2.1/scala/io.html">Akka I/O</a>
