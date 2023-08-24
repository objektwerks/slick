Slick
-----
>Slick feature tests and performance benchmark using H2 and Scala 3.

Scala 3 Bug
-----------
>See this PR ( https://github.com/slick/slick/pull/2760 ). The workaround is
>simple, just define ```def tupled = (apply _).tupled``` in the companion
>object of each case class and it will compile for all Scala versions.

Test
----
1. sbt clean test

Benchmark
---------
>See Performance class for details.
1. sbt jmh:run
>**Warning** Using JDK17, throws: java.lang.ClassNotFoundException: java.sql.Timestamp

Results
-------
>OpenJDK Runtime Environment (Zulu 8.56.0.23-CA-macos-aarch64) (build 1.8.0_302-b08)
1. addRole - 308.046
2. listRoles - 37.703
>Total time: 405 s (06:45), 10 warmups, 10 iterations, in microseconds, completed 2021.9.9
