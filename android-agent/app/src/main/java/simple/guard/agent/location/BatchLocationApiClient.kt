package simple.guard.agent.location

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import simple.guard.agent.pairing.AgentKeyStore
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class BatchLocationApiClient {
    fun send(
        instanceUrl: String,
        deviceId: String,
        agentInstanceId: String,
        events: List<SignedTelemetryEnvelope>,
    ): List<TelemetryBatchItemResult> {
        val connection = openConnection(instanceUrl, deviceId, agentInstanceId)
        writeBatchRequest(connection, events)
        val status = connection.responseCode
        if (status !in 200..299) {
            val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            throw LocationApiException.from(status, errorBody)
        }

        return parseBatchResults(connection.inputStream.bufferedReader().use { it.readText() })
    }

    private fun openConnection(
        instanceUrl: String,
        deviceId: String,
        agentInstanceId: String,
    ): HttpURLConnection {
        val endpoint = URL("${instanceUrl.trimEnd('/')}/api/agent/devices/$deviceId/telemetry/batch")
        return (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Agent-Instance-Id", agentInstanceId)
        }
    }

    private fun writeBatchRequest(
        connection: HttpURLConnection,
        events: List<SignedTelemetryEnvelope>,
    ) {
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(batchRequestBody(events).toString())
        }
    }

    private fun batchRequestBody(events: List<SignedTelemetryEnvelope>): JSONObject =
        JSONObject().put(
            "events",
            JSONArray().apply {
                events.forEach { signed ->
                    put(
                        JSONObject()
                            .put("signature", signed.signature)
                            .put("telemetry", TelemetryJsonCodec.envelopeToJson(signed.envelope)),
                    )
                }
            },
        )

    private fun parseBatchResults(responseBody: String): List<TelemetryBatchItemResult> {
        val results = JSONObject(responseBody).getJSONArray("results")
        return List(results.length()) { index ->
            val result = results.getJSONObject(index)
            TelemetryBatchItemResult(
                eventId = if (result.isNull("eventId")) null else result.getString("eventId"),
                status = TelemetryBatchItemStatus.valueOf(result.getString("status")),
                error = if (result.isNull("error")) null else result.getString("error"),
            )
        }
    }
}

class LocationApiException(val userMessage: String) : RuntimeException(userMessage) {
    companion object {
        fun from(
            status: Int,
            body: String,
        ): LocationApiException {
            val code = runCatching { JSONObject(body).optString("erro_code") }.getOrDefault("")
            return LocationApiException(
                when {
                    status == 401 || code == "DEVICE_CREDENTIAL_INVALID" || code == "DEVICE_CREDENTIAL_REVOKED" ->
                        "A instancia recusou a credencial deste agente."
                    status == 400 -> "A instancia recusou os dados de telemetria."
                    else -> "A instancia nao recebeu a telemetria."
                },
            )
        }
    }
}

class BatchLocationApiSender(
    private val instanceUrl: String,
    private val deviceId: String,
    private val agentInstanceId: String,
    private val keyStore: AgentKeyStore,
    private val apiClient: BatchLocationApiClient,
    private val diagnosticsStore: LocationDiagnosticsStore,
) : TelemetryBatchSender {
    override fun send(
        envelopes: List<TelemetryEnvelope>,
        callback: (TelemetryBatchSendResult) -> Unit,
    ) {
        Thread {
            val result = sendBatch(envelopes)
            callback(result)
        }.start()
    }

    private fun sendBatch(envelopes: List<TelemetryEnvelope>): TelemetryBatchSendResult {
        return runCatching {
            Log.i(TAG, "Enviando lote de telemetria para a API da instancia pareada.")
            apiClient.send(instanceUrl, deviceId, agentInstanceId, signedEnvelopes(envelopes))
        }.fold(
            onSuccess = { results -> handleBatchSuccess(envelopes, results) },
            onFailure = { failure -> handleBatchFailure(envelopes, failure) },
        )
    }

    private fun signedEnvelopes(envelopes: List<TelemetryEnvelope>): List<SignedTelemetryEnvelope> {
        return envelopes.map { envelope ->
            SignedTelemetryEnvelope(
                signature = keyStore.signTelemetry(agentInstanceId, deviceId, envelope),
                envelope = envelope,
            )
        }
    }

    private fun handleBatchSuccess(
        envelopes: List<TelemetryEnvelope>,
        results: List<TelemetryBatchItemResult>,
    ): TelemetryBatchSendResult {
        val envelopesById = envelopes.associateBy(TelemetryEnvelope::eventId)
        results.forEach { item ->
            val envelope = item.eventId?.let(envelopesById::get) ?: return@forEach
            when (item.status) {
                TelemetryBatchItemStatus.ACCEPTED,
                TelemetryBatchItemStatus.DUPLICATE,
                -> diagnosticsStore.recordSendSuccess(envelope)
                TelemetryBatchItemStatus.INVALID,
                TelemetryBatchItemStatus.UNAUTHORIZED,
                TelemetryBatchItemStatus.FAILED,
                -> diagnosticsStore.recordSendFailure(envelope, item.error ?: "A instancia nao confirmou o evento.")
            }
        }
        Log.i(TAG, "Lote de telemetria processado pela instancia.")
        return TelemetryBatchSendResult.Completed(results)
    }

    private fun handleBatchFailure(
        envelopes: List<TelemetryEnvelope>,
        failure: Throwable,
    ): TelemetryBatchSendResult {
        val message =
            if (failure is LocationApiException) {
                failure.userMessage
            } else {
                "Falha de rede ao enviar telemetria."
            }
        Log.e(TAG, message, failure)
        envelopes.firstOrNull()?.let { diagnosticsStore.recordSendFailure(it, message) }
        return TelemetryBatchSendResult.Failed(message)
    }

    private companion object {
        const val TAG = "SimpleGuardLocation"
    }
}
