use crate::pairing::DesktopPairingRequest;
use crate::unpairing::{DesktopPairingIdentity, DesktopUnpairingError};
use reqwest::blocking::Client;
use reqwest::Url;
use std::fmt;
use std::time::Duration;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum DesktopApiError {
    InvalidUrl,
    UnpairingContract(DesktopUnpairingError),
    Io(String),
    HttpStatus(u16, String),
}

impl fmt::Display for DesktopApiError {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::InvalidUrl => formatter.write_str("URL da instancia invalida"),
            Self::UnpairingContract(error) => {
                write!(formatter, "Contrato de despareamento invalido: {:?}", error)
            }
            Self::Io(message) => write!(formatter, "Falha de comunicacao: {}", message),
            Self::HttpStatus(status, body) => {
                write!(formatter, "{}", user_message_from_api_error(*status, body))
            }
        }
    }
}

impl std::error::Error for DesktopApiError {}

impl From<DesktopUnpairingError> for DesktopApiError {
    fn from(value: DesktopUnpairingError) -> Self {
        Self::UnpairingContract(value)
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopApiResponse {
    pub status: u16,
    pub body: String,
}

pub struct DesktopAgentApiClient;

impl DesktopAgentApiClient {
    pub fn complete_pairing(
        instance_url: &str,
        request: &DesktopPairingRequest,
    ) -> Result<DesktopApiResponse, DesktopApiError> {
        let endpoint = endpoint(instance_url, "/api/agent/pairing/complete")?;
        let response = client()?
            .post(endpoint)
            .header("Accept", "application/json")
            .header("Cache-Control", "no-store")
            .header("Content-Type", "application/json")
            .body(request.to_json())
            .send()
            .map_err(|error| DesktopApiError::Io(error.to_string()))?;
        response_from_reqwest(response)
    }

    pub fn request_unpairing(
        identity: &DesktopPairingIdentity,
        signature: &str,
    ) -> Result<DesktopApiResponse, DesktopApiError> {
        identity.validate()?;
        let path = format!("/api/agent/devices/{}/pairing", identity.device_id.trim());
        let endpoint = endpoint(&identity.instance_url, &path)?;
        let response = client()?
            .delete(endpoint)
            .header("Accept", "application/json")
            .header("Cache-Control", "no-store")
            .header("X-Agent-Instance-Id", identity.agent_instance_id.trim())
            .header("X-Agent-Signature", signature.trim())
            .send()
            .map_err(|error| DesktopApiError::Io(error.to_string()))?;
        response_from_reqwest(response)
    }

    pub fn pairing_status(
        identity: &DesktopPairingIdentity,
        signature: &str,
    ) -> Result<DesktopApiResponse, DesktopApiError> {
        identity.validate()?;
        let path = format!("/api/agent/devices/{}/pairing", identity.device_id.trim());
        let endpoint = endpoint(&identity.instance_url, &path)?;
        let response = client()?
            .get(endpoint)
            .header("Accept", "application/json")
            .header("Cache-Control", "no-store")
            .header("X-Agent-Instance-Id", identity.agent_instance_id.trim())
            .header("X-Agent-Signature", signature.trim())
            .send()
            .map_err(|error| DesktopApiError::Io(error.to_string()))?;
        response_from_reqwest(response)
    }
}

pub fn extract_json_string_field(body: &str, field: &str) -> Option<String> {
    let needle = format!("\"{}\"", field);
    let field_start = body.find(&needle)?;
    let after_field = &body[field_start + needle.len()..];
    let colon = after_field.find(':')?;
    let after_colon = after_field[colon + 1..].trim_start();
    if !after_colon.starts_with('"') {
        return None;
    }

    let mut escaped = false;
    let mut value = String::new();
    for character in after_colon[1..].chars() {
        if escaped {
            value.push(match character {
                '"' => '"',
                '\\' => '\\',
                'n' => '\n',
                'r' => '\r',
                't' => '\t',
                other => other,
            });
            escaped = false;
            continue;
        }
        match character {
            '\\' => escaped = true,
            '"' => return Some(value),
            other => value.push(other),
        }
    }
    None
}

fn user_message_from_api_error(status: u16, body: &str) -> String {
    match extract_json_string_field(body, "erro_code").as_deref() {
        Some("DEVICE_ALREADY_PAIRED") => {
            "Este dispositivo ja esta pareado nesta instancia.".to_owned()
        }
        Some("PAIRING_CODE_INVALID") => "Codigo de pareamento invalido.".to_owned(),
        Some("PAIRING_CODE_EXPIRED") => "Codigo de pareamento expirado.".to_owned(),
        Some("DEVICE_CREDENTIAL_INVALID") => {
            "A instancia recusou a credencial deste agente.".to_owned()
        }
        _ => extract_json_string_field(body, "mensagem")
            .or_else(|| extract_json_string_field(body, "message"))
            .unwrap_or_else(|| format!("API retornou HTTP {}", status)),
    }
}

fn client() -> Result<Client, DesktopApiError> {
    Client::builder()
        .timeout(Duration::from_secs(10))
        .build()
        .map_err(|error| DesktopApiError::Io(error.to_string()))
}

fn endpoint(instance_url: &str, path: &str) -> Result<Url, DesktopApiError> {
    let base = Url::parse(instance_url.trim()).map_err(|_| DesktopApiError::InvalidUrl)?;
    if base.scheme() != "http" && base.scheme() != "https" {
        return Err(DesktopApiError::InvalidUrl);
    }
    base.join(path.trim_start_matches('/'))
        .map_err(|_| DesktopApiError::InvalidUrl)
}

fn response_from_reqwest(
    response: reqwest::blocking::Response,
) -> Result<DesktopApiResponse, DesktopApiError> {
    let status = response.status().as_u16();
    let body = response
        .text()
        .map_err(|error| DesktopApiError::Io(error.to_string()))?;

    if (200..300).contains(&status) {
        Ok(DesktopApiResponse { status, body })
    } else {
        Err(DesktopApiError::HttpStatus(status, body))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::pairing::DesktopPlatform;

    #[test]
    fn builds_http_and_https_endpoint_tests() {
        let http = endpoint(
            "http://localhost:8080/simpleguard/",
            "/api/agent/pairing/complete",
        )
        .expect("valid http endpoint");
        let https = endpoint("https://sg.local", "/api/agent/pairing/complete")
            .expect("valid https endpoint");

        assert_eq!(
            "http://localhost:8080/simpleguard/api/agent/pairing/complete",
            http.as_str()
        );
        assert_eq!(
            "https://sg.local/api/agent/pairing/complete",
            https.as_str()
        );
    }

    #[test]
    fn rejects_non_http_instance_url_tests() {
        assert_eq!(
            DesktopApiError::InvalidUrl,
            endpoint("file:///tmp/simpleguard", "/api/agent/pairing/complete").unwrap_err()
        );
    }

    #[test]
    fn extracts_device_name_from_pairing_json_tests() {
        let body = "{\"deviceId\":\"1\",\"deviceName\":\"Notebook Joao\",\"platform\":\"WINDOWS\",\"pairingStatus\":\"paired\"}";

        assert_eq!(
            Some("Notebook Joao".to_owned()),
            extract_json_string_field(body, "deviceName")
        );
    }

    #[test]
    fn builds_pairing_api_request_payload_tests() {
        let request = DesktopPairingRequest {
            pairing_code: "ABCD-2345".to_owned(),
            agent_instance_id: "desktop-windows-note".to_owned(),
            platform: DesktopPlatform::Windows,
            public_key: "key".repeat(30),
        };

        assert!(request.to_json().contains("\"platform\":\"WINDOWS\""));
    }

    #[test]
    fn maps_known_api_error_to_user_message_tests() {
        let body = "{\"erro_code\":\"DEVICE_ALREADY_PAIRED\",\"mensagem\":\"The device is already paired.\"}";

        assert_eq!(
            "Este dispositivo ja esta pareado nesta instancia.",
            DesktopApiError::HttpStatus(409, body.to_owned()).to_string()
        );
    }
}
