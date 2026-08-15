package simple.guard.agent.pairing

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature

class AgentKeyStore {

    fun publicKey(agentInstanceId: String): String {
        val alias = alias(agentInstanceId)
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        if (!keyStore.containsAlias(alias)) {
            val generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()
            generator.initialize(spec)
            generator.generateKeyPair()
        }

        val certificate = keyStore.getCertificate(alias)
        return Base64.encodeToString(certificate.publicKey.encoded, Base64.NO_WRAP)
    }

    fun signUnpairing(agentInstanceId: String, deviceId: String): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val privateKey = keyStore.getKey(alias(agentInstanceId), null) as? PrivateKey
            ?: error("A chave local do agente nao foi encontrada.")
        val payload = "UNPAIR_DEVICE\n$deviceId\n$agentInstanceId".toByteArray(Charsets.UTF_8)
        val signature = Signature.getInstance("SHA256withECDSA").apply {
            initSign(privateKey)
            update(payload)
        }.sign()
        return Base64.encodeToString(signature, Base64.NO_WRAP)
    }

    fun delete(agentInstanceId: String) {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        if (keyStore.containsAlias(alias(agentInstanceId))) {
            keyStore.deleteEntry(alias(agentInstanceId))
        }
    }

    private fun alias(agentInstanceId: String): String = "simpleguard-$agentInstanceId"
}
