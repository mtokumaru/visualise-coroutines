package visualise.coroutines

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import visualise.coroutines.simulation.SimulationRunner
import visualise.coroutines.simulation.scenarios.*

fun Application.configureRouting() {
    routing {
        // Serve static files from resources/static
        staticResources("/", "static") {
            default("static/index.html")
        }

        // WebSocket endpoint for simulation control
        webSocket("/simulation") {
            val logger = call.application.log
            val runner = SimulationRunner()

            // Send welcome message
            val welcomeMessage = mapOf(
                "type" to "CONNECTED",
                "message" to "Connected to simulation server"
            )
            send(Frame.Text(Json.encodeToString(welcomeMessage)))

            try {
                // Launch event broadcaster
                val eventBroadcaster = launch {
                    runner.events.collect { event ->
                        val eventMessage = mapOf(
                            "type" to "SIMULATION_EVENT",
                            "event" to Json.encodeToString(event)
                        )
                        send(Frame.Text(Json.encodeToString(eventMessage)))
                    }
                }

                // Handle incoming commands
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        handleSimulationMessage(text, logger, runner, this)
                    }
                }

                eventBroadcaster.cancel()
            } catch (e: Exception) {
                logger.error("WebSocket error: ${e.localizedMessage}")
            } finally {
                runner.pause()
                logger.info("WebSocket connection closed")
            }
        }

        // Health check endpoint
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}

fun handleSimulationMessage(
    message: String,
    logger: org.slf4j.Logger,
    runner: SimulationRunner,
    session: DefaultWebSocketServerSession
) {
    try {
        val json = Json.decodeFromString<Map<String, String>>(message)
        when (val messageType = json["type"]) {
            "PLAY" -> {
                logger.info("Play command received")
                // Play in background
                session.launch {
                    try {
                        runner.play(runner.loadedScenario ?: SingleLaunchScenario())
                    } catch (e: Exception) {
                        logger.error("Error playing simulation: ${e.message}")
                    }
                }
            }
            "PAUSE" -> {
                logger.info("Pause command received")
                runner.pause()
            }
            "RESET" -> {
                logger.info("Reset command received")
                runner.reset()
            }
            "STEP" -> {
                logger.info("Step command received")
                session.launch {
                    runner.step()
                }
            }
            "SET_SPEED" -> {
                val speed = (json["speed"]?.toDouble()) ?: 1.0
                logger.info("Set speed command received: $speed")
                runner.setSpeed(speed)
            }
            "LOAD_SCENARIO" -> {
                val scenarioName = json["scenario"]
                logger.info("Load scenario command received: $scenarioName")

                val scenario = when (scenarioName) {
                    "single-launch" -> SingleLaunchScenario()
                    "multiple-launches" -> MultipleLaunchesScenario()
                    "launch-vs-async" -> LaunchVsAsyncScenario()
                    else -> SingleLaunchScenario()
                }

                runner.load(scenario)
            }
            "SEEK_TIME" -> {
                val time = json["time"]
                logger.info("Seek time command received: $time")
                // TODO: Implement seek functionality
            }
            else -> {
                logger.warn("Unknown message type: $messageType")
            }
        }
    } catch (e: Exception) {
        logger.error("Error handling message: ${e.localizedMessage}", e)
    }
}
