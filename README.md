Slick
-----
>Slick feature tests and performance benchmark against H2 using Scala 3.

Warning
-------
>Slick support for Scala 3 is still a **WIP**, requiring significant code changes.

Test
----
1. sbt clean test

Benchmark
---------
>See Performance class for details.
1. sbt jmh:run

Results
-------
>OpenJDK Runtime Environment (Zulu 8.56.0.23-CA-macos-aarch64) (build 1.8.0_302-b08)
1. addRole - 308.046
2. listRoles - 37.703
>Total time: 405 s (06:45), 10 warmups, 10 iterations, average time in microseconds, completed 2021.9.9