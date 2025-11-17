package visualise.coroutines

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

fun Application.configureRouting() {
    routing {
        // Serve static files from resources/static
        staticResources("/", "static") {
            default("static/index.html")
        }

        // WebSocket endpoint for simulation control
        webSocket("/simulation") {
            val logger = call.application.log

            // Send welcome message
            val welcomeMessage = mapOf(
                "type" to "CONNECTED",
                "message" to "Connected to simulation server"
            )
            send(Frame.Text(Json.encodeToString(welcomeMessage)))

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        handleSimulationMessage(text, logger)
                    }
                }
            } catch (e: Exception) {
                logger.error("WebSocket error: ${e.localizedMessage}")
            } finally {
                logger.info("WebSocket connection closed")
            }
        }

        // Health check endpoint
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}

suspend fun DefaultWebSocketServerSession.handleSimulationMessage(message: String, logger: org.slf4j.Logger) {
    try {
        val json = Json.decodeFromString<Map<String, Any?>>(message)
        val messageType = json["type"] as? String

        when (messageType) {
            "PLAY" -> {
                // TODO: Start simulation
                logger.info("Play command received")
            }
            "PAUSE" -> {
                // TODO: Pause simulation
                logger.info("Pause command received")
            }
            "RESET" -> {
                // TODO: Reset simulation
                logger.info("Reset command received")
            }
            "STEP" -> {
                // TODO: Step simulation
                logger.info("Step command received")
            }
            "SET_SPEED" -> {
                val speed = json["speed"]
                logger.info("Set speed command received: $speed")
            }
            "LOAD_SCENARIO" -> {
                val scenario = json["scenario"]
                logger.info("Load scenario command received: $scenario")
            }
            "SEEK_TIME" -> {
                val time = json["time"]
                logger.info("Seek time command received: $time")
            }
            else -> {
                logger.warn("Unknown message type: $messageType")
            }
        }
    } catch (e: Exception) {
        logger.error("Error handling message: ${e.localizedMessage}")
    }
}
