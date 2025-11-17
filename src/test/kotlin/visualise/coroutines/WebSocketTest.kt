package visualise.coroutines

import io.ktor.client.plugins.websocket.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebSocketTest {

    @Test
    fun `test WebSocket connection and welcome message`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/simulation") {
            // Receive welcome message
            val frame = incoming.receive()
            assertTrue(frame is Frame.Text)
            val message = frame.readText()

            val json = Json.parseToJsonElement(message).jsonObject
            assertEquals("CONNECTED", json["type"]?.jsonPrimitive?.content)
            assertTrue(json.containsKey("message"))
        }
    }

    @Test
    fun `test sending PLAY command`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/simulation") {
            // Skip welcome message
            incoming.receive()

            // Send PLAY command
            send(Frame.Text("""{"type": "PLAY"}"""))

            // The server should process the command without error
            // (We can verify this in logs, but the connection should remain open)
        }
    }

    @Test
    fun `test sending PAUSE command`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/simulation") {
            // Skip welcome message
            incoming.receive()

            // Send PAUSE command
            send(Frame.Text("""{"type": "PAUSE"}"""))
        }
    }

    @Test
    fun `test sending RESET command`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/simulation") {
            // Skip welcome message
            incoming.receive()

            // Send RESET command
            send(Frame.Text("""{"type": "RESET"}"""))
        }
    }

    @Test
    fun `test sending SET_SPEED command`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/simulation") {
            // Skip welcome message
            incoming.receive()

            // Send SET_SPEED command
            send(Frame.Text("""{"type": "SET_SPEED", "speed": 2.0}"""))
        }
    }

    @Test
    fun `test sending LOAD_SCENARIO command`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/simulation") {
            // Skip welcome message
            incoming.receive()

            // Send LOAD_SCENARIO command
            send(Frame.Text("""{"type": "LOAD_SCENARIO", "scenario": "single-launch"}"""))
        }
    }

    @Test
    fun `test multiple commands in sequence`() = testApplication {
        application {
            module()
        }

        val client = createClient {
            install(WebSockets)
        }

        client.webSocket("/simulation") {
            // Skip welcome message
            incoming.receive()

            // Send multiple commands
            send(Frame.Text("""{"type": "LOAD_SCENARIO", "scenario": "single-launch"}"""))
            send(Frame.Text("""{"type": "PLAY"}"""))
            send(Frame.Text("""{"type": "PAUSE"}"""))
            send(Frame.Text("""{"type": "RESET"}"""))

            // All commands should be processed successfully
        }
    }
}
