Slick
-----
>Slick feature tests and performance benchmark.

Test
----
1. sbt clean test

Benchmark
---------
1. sbt jmh:run

Results
-------
>OpenJDK 64-Bit Server VM (Zulu 8.56.0.23-CA-macos-aarch64) (build 25.302-b08, mixed mode)
1. addRole - 308.046
2. listRoles - 37.703
>Total time: 405 s (06:45), 10 warmups, 10 iterations, in microseconds, completed 2021.9.9