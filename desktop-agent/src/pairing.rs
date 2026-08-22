use std::fmt;

const MIN_PUBLIC_KEY_LENGTH: usize = 64;
const MAX_PUBLIC_KEY_LENGTH: usize = 4096;
const MAX_AGENT_INSTANCE_ID_LENGTH: usize = 128;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum DesktopPlatform {
    Windows,
    Linux,
    Macos,
}

impl DesktopPlatform {
    pub fn api_value(self) -> &'static str {
        match self {
            Self::Windows => "WINDOWS",
            Self::Linux => "LINUX",
            Self::Macos => "MACOS",
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopAgentIdentity {
    agent_instance_id: String,
}

impl DesktopAgentIdentity {
    pub fn from_agent_instance_id(value: impl Into<String>) -> Result<Self, PairingContractError> {
        let agent_instance_id = value.into().trim().to_owned();
        validate_agent_instance_id(&agent_instance_id)?;
        Ok(Self { agent_instance_id })
    }

    pub fn from_local_seed(
        platform: DesktopPlatform,
        machine_seed: impl AsRef<str>,
    ) -> Result<Self, PairingContractError> {
        let seed = sanitize_identifier_component(machine_seed.as_ref());
        if seed.is_empty() {
            return Err(PairingContractError::MissingAgentInstanceId);
        }

        let mut agent_instance_id = format!(
            "desktop-{}-{}",
            platform.api_value().to_ascii_lowercase(),
            seed
        );
        agent_instance_id.truncate(MAX_AGENT_INSTANCE_ID_LENGTH);
        Self::from_agent_instance_id(agent_instance_id)
    }

    pub fn agent_instance_id(&self) -> &str {
        &self.agent_instance_id
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopAgentKey {
    public_key: String,
}

impl DesktopAgentKey {
    pub fn from_public_key(value: impl Into<String>) -> Result<Self, PairingContractError> {
        let public_key = value.into().trim().to_owned();
        validate_public_key(&public_key)?;
        Ok(Self { public_key })
    }

    pub fn public_key(&self) -> &str {
        &self.public_key
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopPairingConfig {
    pub instance_url: String,
    pub pairing_code: String,
    pub platform: DesktopPlatform,
    pub identity: DesktopAgentIdentity,
    pub key: DesktopAgentKey,
}

impl DesktopPairingConfig {
    pub fn validate(&self) -> Result<(), PairingContractError> {
        validate_instance_url(&self.instance_url)?;
        validate_pairing_code(&self.pairing_code)?;
        validate_agent_instance_id(self.identity.agent_instance_id())?;
        validate_public_key(self.key.public_key())?;
        Ok(())
    }

    pub fn complete_pairing_request(&self) -> Result<DesktopPairingRequest, PairingContractError> {
        self.validate()?;
        Ok(DesktopPairingRequest {
            pairing_code: self.pairing_code.trim().to_owned(),
            agent_instance_id: self.identity.agent_instance_id().to_owned(),
            platform: self.platform,
            public_key: self.key.public_key().to_owned(),
        })
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopPairingRequest {
    pub pairing_code: String,
    pub agent_instance_id: String,
    pub platform: DesktopPlatform,
    pub public_key: String,
}

impl DesktopPairingRequest {
    pub fn to_json(&self) -> String {
        format!(
            "{{\"pairingCode\":\"{}\",\"agentInstanceId\":\"{}\",\"platform\":\"{}\",\"publicKey\":\"{}\"}}",
            escape_json_string(&self.pairing_code),
            escape_json_string(&self.agent_instance_id),
            self.platform.api_value(),
            escape_json_string(&self.public_key)
        )
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum PairingContractError {
    MissingInstanceUrl,
    InvalidInstanceUrl,
    MissingPairingCode,
    MissingAgentInstanceId,
    AgentInstanceIdTooLong,
    MissingPublicKey,
    PublicKeyTooShort,
    PublicKeyTooLong,
}

impl fmt::Display for PairingContractError {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        let message = match self {
            Self::MissingInstanceUrl => "instance URL is required",
            Self::InvalidInstanceUrl => "instance URL must start with http:// or https://",
            Self::MissingPairingCode => "pairing code is required",
            Self::MissingAgentInstanceId => "agent instance ID is required",
            Self::AgentInstanceIdTooLong => "agent instance ID is too long",
            Self::MissingPublicKey => "public key is required",
            Self::PublicKeyTooShort => "public key is too short",
            Self::PublicKeyTooLong => "public key is too long",
        };
        formatter.write_str(message)
    }
}

impl std::error::Error for PairingContractError {}

fn validate_instance_url(value: &str) -> Result<(), PairingContractError> {
    let trimmed = value.trim();
    if trimmed.is_empty() {
        return Err(PairingContractError::MissingInstanceUrl);
    }
    if !trimmed.starts_with("http://") && !trimmed.starts_with("https://") {
        return Err(PairingContractError::InvalidInstanceUrl);
    }
    Ok(())
}

fn validate_pairing_code(value: &str) -> Result<(), PairingContractError> {
    if value.trim().is_empty() {
        return Err(PairingContractError::MissingPairingCode);
    }
    Ok(())
}

fn validate_agent_instance_id(value: &str) -> Result<(), PairingContractError> {
    if value.trim().is_empty() {
        return Err(PairingContractError::MissingAgentInstanceId);
    }
    if value.chars().count() > MAX_AGENT_INSTANCE_ID_LENGTH {
        return Err(PairingContractError::AgentInstanceIdTooLong);
    }
    Ok(())
}

fn validate_public_key(value: &str) -> Result<(), PairingContractError> {
    let length = value.trim().chars().count();
    if length == 0 {
        return Err(PairingContractError::MissingPublicKey);
    }
    if length < MIN_PUBLIC_KEY_LENGTH {
        return Err(PairingContractError::PublicKeyTooShort);
    }
    if length > MAX_PUBLIC_KEY_LENGTH {
        return Err(PairingContractError::PublicKeyTooLong);
    }
    Ok(())
}

fn sanitize_identifier_component(value: &str) -> String {
    let normalized = value
        .trim()
        .to_ascii_lowercase()
        .chars()
        .map(|character| {
            if character.is_ascii_alphanumeric() {
                character
            } else {
                '-'
            }
        })
        .collect::<String>();

    normalized
        .split('-')
        .filter(|part| !part.is_empty())
        .collect::<Vec<_>>()
        .join("-")
}

fn escape_json_string(value: &str) -> String {
    let mut escaped = String::with_capacity(value.len());
    for character in value.chars() {
        match character {
            '"' => escaped.push_str("\\\""),
            '\\' => escaped.push_str("\\\\"),
            '\n' => escaped.push_str("\\n"),
            '\r' => escaped.push_str("\\r"),
            '\t' => escaped.push_str("\\t"),
            control if control.is_control() => {
                escaped.push_str(&format!("\\u{:04x}", control as u32));
            }
            other => escaped.push(other),
        }
    }
    escaped
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn builds_desktop_pairing_request_from_valid_local_config_tests() {
        let config = valid_config_tests(DesktopPlatform::Windows);

        let request = config.complete_pairing_request().expect("valid request");

        assert_eq!("ABCD-2345", request.pairing_code);
        assert_eq!("desktop-windows-machine-01", request.agent_instance_id);
        assert_eq!(DesktopPlatform::Windows, request.platform);
        assert_eq!(public_key_tests(), request.public_key);
    }

    #[test]
    fn serializes_desktop_pairing_request_using_agent_contract_tests() {
        let request = valid_config_tests(DesktopPlatform::Linux)
            .complete_pairing_request()
            .expect("valid request");

        assert_eq!(
            format!(
                "{{\"pairingCode\":\"ABCD-2345\",\"agentInstanceId\":\"desktop-linux-machine-01\",\"platform\":\"LINUX\",\"publicKey\":\"{}\"}}",
                public_key_tests()
            ),
            request.to_json()
        );
    }

    #[test]
    fn escapes_json_string_values_tests() {
        let request = DesktopPairingRequest {
            pairing_code: "AB\"D\n2345".to_owned(),
            agent_instance_id: "desktop-windows-machine-01".to_owned(),
            platform: DesktopPlatform::Windows,
            public_key: public_key_tests(),
        };

        assert!(request.to_json().contains("AB\\\"D\\n2345"));
    }

    #[test]
    fn creates_local_identity_from_platform_and_machine_seed_tests() {
        let identity =
            DesktopAgentIdentity::from_local_seed(DesktopPlatform::Macos, "Joao Vitor MacBook Pro")
                .expect("valid identity");

        assert_eq!(
            "desktop-macos-joao-vitor-macbook-pro",
            identity.agent_instance_id()
        );
    }

    #[test]
    fn rejects_missing_local_identity_seed_tests() {
        let error = DesktopAgentIdentity::from_local_seed(DesktopPlatform::Linux, "   ")
            .expect_err("missing identity");

        assert_eq!(PairingContractError::MissingAgentInstanceId, error);
    }

    #[test]
    fn rejects_missing_instance_url_tests() {
        let mut config = valid_config_tests(DesktopPlatform::Windows);
        config.instance_url = " ".to_owned();

        assert_eq!(
            PairingContractError::MissingInstanceUrl,
            config.complete_pairing_request().expect_err("missing URL")
        );
    }

    #[test]
    fn rejects_invalid_instance_url_tests() {
        let mut config = valid_config_tests(DesktopPlatform::Windows);
        config.instance_url = "simpleguard.local".to_owned();

        assert_eq!(
            PairingContractError::InvalidInstanceUrl,
            config.complete_pairing_request().expect_err("invalid URL")
        );
    }

    #[test]
    fn rejects_missing_pairing_code_tests() {
        let mut config = valid_config_tests(DesktopPlatform::Windows);
        config.pairing_code = " ".to_owned();

        assert_eq!(
            PairingContractError::MissingPairingCode,
            config.complete_pairing_request().expect_err("missing code")
        );
    }

    #[test]
    fn rejects_missing_public_key_tests() {
        let error = DesktopAgentKey::from_public_key(" ").expect_err("missing public key");

        assert_eq!(PairingContractError::MissingPublicKey, error);
    }

    #[test]
    fn rejects_short_public_key_tests() {
        let error = DesktopAgentKey::from_public_key("short-key").expect_err("short public key");

        assert_eq!(PairingContractError::PublicKeyTooShort, error);
    }

    fn valid_config_tests(platform: DesktopPlatform) -> DesktopPairingConfig {
        DesktopPairingConfig {
            instance_url: "http://simpleguard.local".to_owned(),
            pairing_code: " ABCD-2345 ".to_owned(),
            platform,
            identity: DesktopAgentIdentity::from_local_seed(platform, "machine-01")
                .expect("valid identity"),
            key: DesktopAgentKey::from_public_key(public_key_tests()).expect("valid key"),
        }
    }

    fn public_key_tests() -> String {
        format!("desktop-public-key-{}", "A".repeat(80))
    }
}
