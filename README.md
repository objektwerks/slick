Slick
-----
>Slick feature tests and performance benchmark against H2 using Scala 3.

Test
----
1. sbt clean test

Benchmark
---------
>See Performance class for details.
1. sbt jmh:run
>**Warning:** Using JDK9+, throws: java.lang.ClassNotFoundException: java.sql.ResultSet

>**See:** [Benchmark compilation fails if java.sql.ResultSet is used #192](https://github.com/sbt/sbt-jmh/issues/192)

>**Update:** Using JDK 21 and sbt-jmh 46, throws: java.lang.ClassNotFoundException: javax.sql.Timestamp

Results
-------
>OpenJDK Runtime Environment (Zulu 8.56.0.23-CA-macos-aarch64) (build 1.8.0_302-b08)
1. addRole - 308.046
2. listRoles - 37.703
>Total time: 405 s (06:45), 10 warmups, 10 iterations, average time in microseconds, completed 2021.9.9
