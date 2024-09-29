package coroutines

import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.security.SecureRandom
import kotlin.system.measureTimeMillis

@Component
class ParallelCalls {

    companion object {
        val HANDLER =
            CoroutineExceptionHandler { context, throwable -> println("throwable: $throwable, jobContext: ${context[Job]}") }
        val COROUTINESCOPE = CoroutineScope(SupervisorJob() + Dispatchers.IO + HANDLER)
        var numOfTimesCalled = 0
    }


    fun makeCallsInParallel() = runBlocking {
        measureTimeMillis {
            COROUTINESCOPE.launch {
                supervisorScope {
                    repeat(20) {
                        launch {
                            callMethod()
                        }
                    }
                }
            }.join()
        }.also {
            println("All coroutines processed in $it seconds")
            print(numOfTimesCalled)
        }
    }

    fun makeCallsInParallel2() = runBlocking {
        measureTimeMillis {
            supervisorScope {
                repeat(20) {
                    launch {
                        COROUTINESCOPE.launch { callMethod() }.join()
                    }
                }
            }
        }.also {
            println("All coroutines processed in $it seconds")
            print(numOfTimesCalled)
        }
    }

    fun makeCallsInParallel3() = runBlocking {
        val namespaces = mutableListOf<String>()
        measureTimeMillis {
            val result = mutableListOf<String>()
            supervisorScope {
                repeat(20) {
                    launch {
                        kotlin.runCatching {
                            result.add(COROUTINESCOPE.async { callMethod() }.await())
                        }.getOrElse {
                            namespaces.add(it.message!!)
                        }

                    }
                }
            }

            result.forEach {
                println(it)
            }

            println("Regras que precisam ser reprocessadas: $namespaces")
        }.also {
            println("All coroutines processed in $it seconds")
            print(numOfTimesCalled)
        }
    }


    suspend fun callMethod(): String {
        numOfTimesCalled++
        return russianRoulleteBlocking(SecureRandom().nextInt(1, 100))

    }

    fun russianRoulleteBlocking(number: Int): String {
        val regraParaReprocessar = retornaNomeRegra()
        val latency = SecureRandom().nextInt(1, 6)
        if (number % 2 == 0) {
            Thread.sleep((latency * 5000).toLong())
            println("Network call made with success in $latency seconds, choosen number: $number")
            return "Network call made with success in $latency seconds, choosen number: $number"
        }
//        throw RuntimeException(regraParaReprocessar)
        throw RuntimeException("I/O error while making network call, choosen number $number")
    }

    private fun retornaNomeRegra(): String {
        val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        val salt = StringBuilder()

        while (salt.length < 10) { // length of the random string.
            val index = (SecureRandom().nextInt(SALTCHARS.length))
            salt.append(SALTCHARS.elementAt(index))
        }
        val saltStr  = salt.toString()
        return saltStr

    }
}