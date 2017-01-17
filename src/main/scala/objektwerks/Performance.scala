package objektwerks

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Fork(1)
class Performance() {
  import Store.repository._

  @Benchmark
  def role(): Int = await(addRole(Role(UUID.randomUUID.toString)))
}