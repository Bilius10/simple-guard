package simple.guard.agent.location

import android.util.Log
import org.json.JSONObject
import simple.guard.agent.pairing.AgentKeyStore
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LocationApiClient {

    fun send(
        instanceUrl: String,
        deviceId: String,
        agentInstanceId: String,
        signature: String,
        reading: LocationReading
    ) {
        val endpoint = URL("${instanceUrl.trimEnd('/')}/api/agent/devices/$deviceId/locations")
        val connection = endpoint.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("X-Agent-Instance-Id", agentInstanceId)
        connection.setRequestProperty("X-Agent-Signature", signature)

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(reading.toJson().toString())
        }

        val status = connection.responseCode
        if (status !in 200..299) {
            val body = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            throw LocationApiException.from(status, body)
        }
        connection.inputStream.close()
    }

    private fun LocationReading.toJson(): JSONObject = JSONObject()
        .put("latitude", latitude)
        .put("longitude", longitude)
        .put("accuracyMeters", accuracyMeters)
        .put("altitudeMeters", altitudeMeters)
        .put("speedMetersPerSecond", speedMetersPerSecond)
        .put("provider", provider)
        .put("collectedAt", collectedAt.toString())
}

class LocationApiException(val userMessage: String) : RuntimeException(userMessage) {

    companion object {
        fun from(status: Int, body: String): LocationApiException {
            val code = runCatching { JSONObject(body).optString("erro_code") }.getOrDefault("")
            return LocationApiException(when {
                status == 401 || code == "DEVICE_CREDENTIAL_INVALID" || code == "DEVICE_CREDENTIAL_REVOKED" -> {
                    "A instancia recusou a credencial deste agente."
                }
                status == 400 -> "A instancia recusou os dados de localizacao."
                else -> "A instancia nao recebeu a localizacao."
            })
        }
    }
}

class LocationApiSender(
    private val instanceUrl: String,
    private val deviceId: String,
    private val agentInstanceId: String,
    private val keyStore: AgentKeyStore,
    private val apiClient: LocationApiClient,
    private val diagnosticsStore: LocationDiagnosticsStore
) : LocationSender {

    override fun send(reading: LocationReading, callback: (LocationSendResult) -> Unit) {
        Thread {
            val result = runCatching {
                Log.i(TAG, "Enviando localizacao para a API da instancia pareada.")
                apiClient.send(
                    instanceUrl,
                    deviceId,
                    agentInstanceId,
                    keyStore.signLocation(agentInstanceId, deviceId, reading),
                    reading
                )
            }.fold(
                onSuccess = {
                    Log.i(TAG, "Localizacao enviada com sucesso.")
                    diagnosticsStore.recordSendSuccess(reading.provider)
                    LocationSendResult.Sent
                },
                onFailure = { failure ->
                    val message = if (failure is LocationApiException) {
                        failure.userMessage
                    } else {
                        "Falha de rede ao enviar localizacao."
                    }
                    Log.e(TAG, message, failure)
                    diagnosticsStore.recordSendFailure(reading.provider, message)
                    LocationSendResult.Failed
                }
            )
            callback(result)
        }.start()
    }

    private companion object {
        const val TAG = "SimpleGuardLocation"
    }
}
