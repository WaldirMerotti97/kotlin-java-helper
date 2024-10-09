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
        val COROUTINESCOPE = CoroutineScope(SupervisorJob() + Dispatchers.IO + HANDLER) // try to replace SupervisorJob for Job and see the results :P
        var numOfTimesCalled = 0
    }

    fun makeCallsInParallel5() = runBlocking {
        val namespacesReprocessar = mutableListOf<String>()
        val regrasComSucesso = mutableListOf<String>()
        measureTimeMillis {
            val deferredList = retornaListaRegras().map {
                ResultadoRegra(regra = it, coroutine = COROUTINESCOPE.async { callMethod() })
            }
            deferredList.forEach {
                kotlin.runCatching {
                    val retornoIO = it.coroutine.await()
                    val regra = it.regra.nome
                    regrasComSucesso.add(regra)
                }.getOrElse { throwable ->
                    println("Class: ${throwable.javaClass.name}, Message: ${throwable.message}")
                    namespacesReprocessar.add(it.regra.nome)
                }
            }
        }.also {
            println("All coroutines processed in $it seconds")
            println("Num of times called: $numOfTimesCalled")
        }
        println("Regras com sucesso $regrasComSucesso")

        println("Regras que precisam ser reprocessadas: $namespacesReprocessar")
    }


    suspend fun callMethod(): String {
        numOfTimesCalled++
        return russianRoulleteNonBlocking(SecureRandom().nextInt(1, 100))

    }

    suspend fun russianRoulleteNonBlocking(number: Int): String {
        val latency = SecureRandom().nextInt(1, 6)
        if (number % 2 == 0) {
            delay(latency.toLong() * 2000)
            println("Network call made with success in $latency seconds, choosen number: $number")
            return "Retorno api externa"
        }
        throw RuntimeException("I/O error while making network call, choosen number $number")
    }

    private fun retornaListaRegras(): List<Regra> {
        val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        val regras = mutableListOf<Regra>()

        for (i in 1..20) {
            val salt = StringBuilder()

            while (salt.length < 10) { // length of the random string.
                val index = (SecureRandom().nextInt(SALTCHARS.length))
                salt.append(SALTCHARS.elementAt(index))
            }
            regras.add(
                Regra(
                    salt.toString(),
                    if (SecureRandom().nextInt(1, 100) > 50) "APROVADA" else "REPROVADA"
                )
            )
        }

        return regras

    }

}

data class ResultadoRegra(val regra: Regra, val coroutine: Deferred<String>)

data class Regra(
    val nome: String,
    val situacao: String,
)
