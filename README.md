Slick
-----
>The purpose of this project is to test Slick features.

Test
----
1. sbt clean test

Benchmark
---------
1. sbt clean compile jmh:run -i 20 -wi 10 -f1 -t1
