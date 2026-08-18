use base64::{engine::general_purpose::STANDARD, Engine as _};
use p256::ecdsa::signature::Signer;
use p256::ecdsa::{Signature, SigningKey};
use p256::pkcs8::{DecodePrivateKey, EncodePrivateKey, EncodePublicKey};
use rand_core::OsRng;
use std::fmt;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum DesktopKeyStoreError {
    Encode,
    Decode,
}

impl fmt::Display for DesktopKeyStoreError {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Encode => formatter.write_str("Falha ao codificar chave local do agente"),
            Self::Decode => formatter.write_str("Falha ao carregar chave local do agente"),
        }
    }
}

impl std::error::Error for DesktopKeyStoreError {}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopAgentKeyPair {
    pub public_key: String,
    pub private_key: String,
}

impl DesktopAgentKeyPair {
    pub fn generate() -> Result<Self, DesktopKeyStoreError> {
        let signing_key = SigningKey::random(&mut OsRng);
        let public_key = signing_key
            .verifying_key()
            .to_public_key_der()
            .map_err(|_| DesktopKeyStoreError::Encode)?;
        let private_key = signing_key
            .to_pkcs8_der()
            .map_err(|_| DesktopKeyStoreError::Encode)?;

        Ok(Self {
            public_key: STANDARD.encode(public_key.as_bytes()),
            private_key: STANDARD.encode(private_key.as_bytes()),
        })
    }

    pub fn sign_unpairing(
        private_key: &str,
        device_id: &str,
        agent_instance_id: &str,
    ) -> Result<String, DesktopKeyStoreError> {
        let private_key_der = STANDARD
            .decode(private_key.trim())
            .map_err(|_| DesktopKeyStoreError::Decode)?;
        let signing_key = SigningKey::from_pkcs8_der(&private_key_der)
            .map_err(|_| DesktopKeyStoreError::Decode)?;
        let payload = format!(
            "UNPAIR_DEVICE\n{}\n{}",
            device_id.trim(),
            agent_instance_id.trim()
        );
        let signature: Signature = signing_key.sign(payload.as_bytes());
        Ok(STANDARD.encode(signature.to_der().as_bytes()))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn generates_base64_public_and_private_key_tests() {
        let key_pair = DesktopAgentKeyPair::generate().expect("key pair");

        assert!(key_pair.public_key.len() >= 64);
        assert!(key_pair.private_key.len() >= 64);
    }

    #[test]
    fn signs_unpairing_payload_tests() {
        let key_pair = DesktopAgentKeyPair::generate().expect("key pair");
        let signature = DesktopAgentKeyPair::sign_unpairing(
            &key_pair.private_key,
            "00000000-0000-0000-0000-000000000000",
            "desktop-windows-note",
        )
        .expect("signature");

        assert!(signature.len() >= 64);
    }
}
