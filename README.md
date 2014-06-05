SKPush-Server
=============

What
-----
<i>SKPush</i> is an lightwight push system which handles tcp requests reactively. 
Support push message to group by tag, and each connection can have multiple tags


Why
----
When you reached to limits of your synchronous application, you will start to look for
an alternative way to expand your request handling capacity. That's where reactive programming will shine.

How
---
By using <a href="http://akka.io/">Akka's</a> actor structure.


With
-----
* <a href="http://spray.io/documentation/1.2-M8/spray-client/">spray-client</a>
* <a href="http://spray.io/documentation/1.2-M8/spray-can/">spray-can</a>
* <a href="https://github.com/mauricio/postgresql-async">postgresql/mysql-async</a>
* <a href="http://doc.akka.io/docs/akka/2.2.1/scala/io.html">Akka I/O</a>


TODO
-----
* Actor Crash Handle
