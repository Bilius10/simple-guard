package simple.guard.agent.unpairing

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class DeviceUnpairingRequestResponse(
    val requestId: String,
    val deviceId: String,
    val deviceName: String,
    val agentInstanceId: String,
    val status: String
)

data class AgentPairingStatusResponse(
    val deviceId: String,
    val pairingStatus: String,
    val unpairingStatus: String
)

class UnpairingApiClient {

    fun unpair(
        instanceUrl: String,
        deviceId: String,
        agentInstanceId: String,
        signature: String
    ): DeviceUnpairingRequestResponse {
        val endpoint = URL("${instanceUrl.trimEnd('/')}/api/agent/devices/$deviceId/pairing")
        val connection = endpoint.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("X-Agent-Instance-Id", agentInstanceId)
        connection.setRequestProperty("X-Agent-Signature", signature)

        val status = connection.responseCode
        val body = when {
            status in 200..299 -> connection.inputStream.bufferedReader().use { it.readText() }
            else -> connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
        if (status !in 200..299) {
            throw UnpairingApiException.from(status, body)
        }

        return parseUnpairingRequestResponse(body)
    }

    fun pairingStatus(
        instanceUrl: String,
        deviceId: String,
        agentInstanceId: String,
        signature: String
    ): AgentPairingStatusResponse {
        val endpoint = URL("${instanceUrl.trimEnd('/')}/api/agent/devices/$deviceId/pairing")
        val connection = endpoint.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Cache-Control", "no-store")
        connection.setRequestProperty("X-Agent-Instance-Id", agentInstanceId)
        connection.setRequestProperty("X-Agent-Signature", signature)

        val status = connection.responseCode
        val body = when {
            status in 200..299 -> connection.inputStream.bufferedReader().use { it.readText() }
            else -> connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
        if (status !in 200..299) {
            throw UnpairingApiException.from(status, body)
        }

        return parseAgentPairingStatusResponse(body)
    }
}

internal fun parseUnpairingRequestResponse(body: String): DeviceUnpairingRequestResponse {
    val json = JSONObject(body)
    return DeviceUnpairingRequestResponse(
        requestId = json.optString("requestId"),
        deviceId = json.optString("deviceId"),
        deviceName = json.optString("deviceName"),
        agentInstanceId = json.optString("agentInstanceId"),
        status = json.optString("status")
    )
}

internal fun parseAgentPairingStatusResponse(body: String): AgentPairingStatusResponse {
    val json = JSONObject(body)
    return AgentPairingStatusResponse(
        deviceId = json.optString("deviceId"),
        pairingStatus = json.optString("pairingStatus"),
        unpairingStatus = json.optString("unpairingStatus")
    )
}

internal object UnpairingRequestContract {

    fun requirePendingRequest(response: DeviceUnpairingRequestResponse): DeviceUnpairingRequestResponse {
        if (response.status != "pending") {
            throw UnpairingApiException("A instancia retornou um estado de despareamento inesperado.")
        }
        return response
    }
}

class UnpairingApiException(val userMessage: String) : RuntimeException(userMessage) {

    companion object {
        fun from(status: Int, body: String): UnpairingApiException {
            val code = runCatching { JSONObject(body).optString("erro_code") }.getOrDefault("")
            return UnpairingApiException(when {
                status == 401 || code == "DEVICE_CREDENTIAL_INVALID" -> {
                    "A instancia recusou a credencial deste agente."
                }
                status == 404 || code == "DEVICE_NOT_FOUND" -> {
                    "O dispositivo nao existe mais nesta instancia."
                }
                else -> {
                    "A instancia nao concluiu o despareamento."
                }
            })
        }
    }
}
