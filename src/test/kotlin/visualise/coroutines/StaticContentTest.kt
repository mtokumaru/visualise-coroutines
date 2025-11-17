package visualise.coroutines

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StaticContentTest {

    @Test
    fun `test index page is served`() = testApplication {
        application {
            module()
        }

        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Text.Html.withCharset(Charsets.UTF_8), response.contentType())

        val content = response.bodyAsText()
        assertTrue(content.contains("Kotlin Coroutines Visualizer"))
        assertTrue(content.contains("<canvas id=\"viz-canvas\">"))
    }

    @Test
    fun `test CSS file is served`() = testApplication {
        application {
            module()
        }

        val response = client.get("/css/styles.css")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Text.CSS.withCharset(Charsets.UTF_8), response.contentType())

        val content = response.bodyAsText()
        assertTrue(content.contains("--bg-primary"))
        assertTrue(content.contains(".visualization-area"))
    }

    @Test
    fun `test JavaScript files are served`() = testApplication {
        application {
            module()
        }

        // Test main.js
        val mainJsResponse = client.get("/js/main.js")
        assertEquals(HttpStatusCode.OK, mainJsResponse.status)
        val mainContent = mainJsResponse.bodyAsText()
        assertTrue(mainContent.contains("CoroutinesVisualizer"))

        // Test visualizer.js
        val vizJsResponse = client.get("/js/visualizer.js")
        assertEquals(HttpStatusCode.OK, vizJsResponse.status)
        val vizContent = vizJsResponse.bodyAsText()
        assertTrue(vizContent.contains("class Visualizer"))

        // Test websocket-client.js
        val wsJsResponse = client.get("/js/websocket-client.js")
        assertEquals(HttpStatusCode.OK, wsJsResponse.status)
        val wsContent = wsJsResponse.bodyAsText()
        assertTrue(wsContent.contains("WebSocketClient"))
    }

    @Test
    fun `test health endpoint`() = testApplication {
        application {
            module()
        }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        val content = response.bodyAsText()
        assertTrue(content.contains("\"status\""))
        assertTrue(content.contains("\"ok\""))
    }

    @Test
    fun `test non-existent file returns 404`() = testApplication {
        application {
            module()
        }

        val response = client.get("/non-existent-file.html")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
