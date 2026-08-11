package simple.guard.agent.pairing

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PairingApiClient {

    fun complete(instanceUrl: String, request: CompletePairingRequest): CompletePairingResponse {
        val endpoint = URL("${instanceUrl.trimEnd('/')}/api/agent/pairing/complete")
        val connection = endpoint.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(request.toJson().toString())
        }

        val status = connection.responseCode
        val body = when {
            status in 200..299 -> connection.inputStream.bufferedReader().use { it.readText() }
            else -> connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }

        if (status !in 200..299) {
            throw PairingApiException.from(status, body)
        }

        val json = JSONObject(body)
        return CompletePairingResponse(
            deviceId = json.optString("deviceId"),
            deviceName = json.optString("deviceName"),
            pairingStatus = json.optString("pairingStatus")
        )
    }

    private fun CompletePairingRequest.toJson(): JSONObject = JSONObject()
        .put("pairingCode", pairingCode.trim())
        .put("agentInstanceId", agentInstanceId.trim())
        .put("platform", platform)
        .put("publicKey", publicKey.trim())
}

class PairingApiException(
    val userMessage: String,
    val expired: Boolean = false
) : RuntimeException(userMessage) {

    companion object {
        fun from(status: Int, body: String): PairingApiException {
            val code = runCatching { JSONObject(body).optString("erro_code") }.getOrDefault("")
            if (status == 410 || code == "PAIRING_SESSION_EXPIRED") {
                return PairingApiException(
                    "O codigo expirou. Gere um novo codigo na plataforma web.",
                    expired = true
                )
            }

            return PairingApiException(when {
                status == 404 || code == "PAIRING_SESSION_INVALID" -> {
                    "Codigo invalido para esta instancia."
                }
                status == 409 || code == "DEVICE_ALREADY_PAIRED" -> {
                    "Este dispositivo ja foi pareado."
                }
                status == 400 -> {
                    "Dados de pareamento incompletos."
                }
                else -> {
                    "A instancia recusou o pareamento."
                }
            })
        }
    }
}
