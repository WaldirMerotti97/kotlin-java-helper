package coroutines

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoroutinesApplication(
	private val parallelCalls: ParallelCalls,
	private val launchExceptionHandling: LaunchExceptionHandling
): CommandLineRunner{
	override fun run(vararg args: String?) {
		parallelCalls.makeCallsInParallel5()
	}

}

fun main(args: Array<String>) {
	runApplication<CoroutinesApplication>(*args)
}
