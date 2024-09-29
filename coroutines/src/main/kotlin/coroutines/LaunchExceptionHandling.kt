package coroutines

import kotlinx.coroutines.*
import org.springframework.stereotype.Component

@Component
class LaunchExceptionHandling {

    companion object {
        val HANDLER =
            CoroutineExceptionHandler { context, throwable -> println("throwable: $throwable, jobContext: ${context[Job]}") }
    }

    fun runParallel() = runBlocking {
        val parentJob = CoroutineScope(SupervisorJob() + Dispatchers.IO + HANDLER).launch {
            supervisorScope {
                launch {
                    throw RuntimeException("Child coroutine failed!")
                }

                launch {
                    throw RuntimeException("Child coroutine failed!")
                }

                launch {
                    println("Cheguei aqui")
                }
            }
        }

        parentJob.invokeOnCompletion { throwable ->
            if (throwable != null) {
                println("Parent job failed: ${throwable}")
            } else {
                println("Parent job SUCCESS")
            }
        }

        parentJob.join() // Wait for the parent job to finish
    }
}