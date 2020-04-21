import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

//fun main(args: Array<String>) {
//    val server = embeddedServer(Netty, port = 8080) {
//        routing {
//            get("/") {
//                call.respondText("Hello World!", ContentType.Text.Plain)
//            }
//            get("/demo") {
//                call.respondText("HELLO WORLD!")
//            }
//        }
//    }
//    server.start(wait = true)
//}

fun hello(): String {
    return "Hello World!"
}

data class Result(val operation: String, val first: Int, val second: Int, val result: Int)
data class CalculatorRequest(
    val operation: String, val first: Int, val second: Int
) {
    val result: Result
    init {
        val mathResult = when (operation) {
            "add" -> first + second
            "multiple" -> first * second
            else -> throw Exception("$operation is not supported")
        }
        result = Result(operation, first, second, mathResult)
    }
}



fun Application.adder() {
    val counts: MutableMap<String, Int> = mutableMapOf()
    install(ContentNegotiation) {
        gson { }
    }
    routing {
        get("/") {
            call.respondText(hello())
        }
        get("/count/{first}") {
            val firstCount = counts.getOrDefault(call.parameters["first"], 0) + 1
            counts[call.parameters["first"].toString()] = firstCount
            println(call.parameters["first"] + ": $firstCount")
            call.respondText(firstCount.toString())
        }
        post("/calculate") {
            try {
                val request = call.receive<CalculatorRequest>()
                val result = when (request.operation) {
                    "add" -> request.first + request.second
                    "subtract" -> request.first - request.second
                    "multiply" -> request.first * request.second
                    "divide" -> request.first / request.second
                    else -> throw Exception("${request.operation} is not supported")
                }
                var response = Result(request.operation, request.first, request.second, result)
                call.respond(response)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }

        }
        get("/{operation}/{first}/{second}") {
            try {
                val operation = call.parameters["operation"]!!
                val first = call.parameters["first"]!!.toInt()
                val second = call.parameters["second"]!!.toInt()
                val result = when (operation) {
                    "add" -> first + second
                    "subtract" -> first - second
                    "multiply" -> first * second
                    "divide" -> first / second
                    else -> throw Exception("$operation is not supported")
                }
                val operationResult = Result(operation, first, second, result)
                call.respond(operationResult)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e)
            }
        }
        get("/show/{first}") {
            try {
                val url = "https://covid19.illinois.edu/";
                println(url)
                //println(call.parameters["first"] + ": $url")
                call.respondText(url)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e)
            }
        }
    }
}
fun main() {
    embeddedServer(Netty, 8080, module = Application::adder).start(wait = true)
}