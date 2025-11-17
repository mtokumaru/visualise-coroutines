package visualise.coroutines

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.websocket.*
import io.ktor.http.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Install plugins
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024)
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // For development - should be restricted in production
    }

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        pingPeriod = kotlin.time.Duration.parse("15s")
        timeout = kotlin.time.Duration.parse("15s")
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    configureRouting()
}
