Slick
-----
>Slick feature tests and performance benchmark against H2 using Scala 3.

Build
-----
1. sbt clean compile

Test
----
1. sbt clean test

Benchmark
---------
>See Performance class for details.
1. sbt jmh:run

Results
-------
>OpenJDK Runtime Environment Zulu22.28+91-CA (build 22+36)
1. addRole - 244.893
2. listRoles - 34.638
>Total time: 403 s (06:43), 10 warmups, 10 iterations, average time in microseconds, completed 2024.4.30

>OpenJDK Runtime Environment (Zulu 8.56.0.23-CA-macos-aarch64) (build 1.8.0_302-b08)
1. addRole - 308.046
2. listRoles - 37.703
>Total time: 405 s (06:45), 10 warmups, 10 iterations, average time in microseconds, completed 2021.9.9