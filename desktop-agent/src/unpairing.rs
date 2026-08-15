#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopPairingIdentity {
    pub device_id: String,
    pub device_name: String,
    pub instance_url: String,
    pub agent_instance_id: String,
}

impl DesktopPairingIdentity {
    pub fn validate(&self) -> Result<(), DesktopUnpairingError> {
        if self.device_id.trim().is_empty() {
            return Err(DesktopUnpairingError::MissingDeviceId);
        }
        if self.device_name.trim().is_empty() {
            return Err(DesktopUnpairingError::MissingDeviceName);
        }
        if !is_http_url(&self.instance_url) {
            return Err(DesktopUnpairingError::InvalidInstanceUrl);
        }
        if self.agent_instance_id.trim().is_empty() {
            return Err(DesktopUnpairingError::MissingAgentInstanceId);
        }
        Ok(())
    }

    pub fn signature_payload(&self) -> Result<Vec<u8>, DesktopUnpairingError> {
        self.validate()?;
        Ok(format!(
            "UNPAIR_DEVICE\n{}\n{}",
            self.device_id.trim(),
            self.agent_instance_id.trim()
        )
        .into_bytes())
    }

    pub fn endpoint(&self) -> Result<String, DesktopUnpairingError> {
        self.validate()?;
        Ok(format!(
            "{}/api/agent/devices/{}/pairing",
            self.instance_url.trim().trim_end_matches('/'),
            self.device_id.trim()
        ))
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum DesktopUnpairingStage {
    ConfirmationRequired,
    Requested,
    Unpaired,
    ApiFailure,
    SyncPending,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopUnpairingViewState {
    pub stage: DesktopUnpairingStage,
    pub badge: &'static str,
    pub detail: String,
    pub destructive_action_enabled: bool,
}

pub struct DesktopUnpairingController;

impl DesktopUnpairingController {
    pub fn confirmation_required(identity: &DesktopPairingIdentity) -> DesktopUnpairingViewState {
        DesktopUnpairingViewState {
            stage: DesktopUnpairingStage::ConfirmationRequired,
            badge: "CONFIRMACAO EXIGIDA",
            detail: format!(
                "Remover {} de {} interrompe telemetria e comandos.",
                identity.device_name, identity.instance_url
            ),
            destructive_action_enabled: true,
        }
    }

    pub fn requested() -> DesktopUnpairingViewState {
        DesktopUnpairingViewState {
            stage: DesktopUnpairingStage::Requested,
            badge: "DESPAREAMENTO SOLICITADO",
            detail: "A instancia esta revogando as credenciais do agente.".to_owned(),
            destructive_action_enabled: false,
        }
    }

    pub fn unpaired() -> DesktopUnpairingViewState {
        DesktopUnpairingViewState {
            stage: DesktopUnpairingStage::Unpaired,
            badge: "DESPAREADO",
            detail: "Vinculo e credenciais locais removidos.".to_owned(),
            destructive_action_enabled: false,
        }
    }

    pub fn api_failure(message: impl Into<String>) -> DesktopUnpairingViewState {
        DesktopUnpairingViewState {
            stage: DesktopUnpairingStage::ApiFailure,
            badge: "FALHA API",
            detail: message.into(),
            destructive_action_enabled: true,
        }
    }

    pub fn sync_pending() -> DesktopUnpairingViewState {
        DesktopUnpairingViewState {
            stage: DesktopUnpairingStage::SyncPending,
            badge: "SYNC PENDENTE",
            detail: "Vinculo local removido; revogacao aguardando conexao.".to_owned(),
            destructive_action_enabled: true,
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum DesktopUnpairingError {
    MissingDeviceId,
    MissingDeviceName,
    InvalidInstanceUrl,
    MissingAgentInstanceId,
}

fn is_http_url(value: &str) -> bool {
    let normalized = value.trim().to_ascii_lowercase();
    normalized.starts_with("http://") || normalized.starts_with("https://")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn builds_unpairing_endpoint_and_signature_payload_tests() {
        let identity = valid_identity_tests();

        assert_eq!(
            "https://simpleguard.local/api/agent/devices/device-001/pairing",
            identity.endpoint().expect("valid endpoint")
        );
        assert_eq!(
            b"UNPAIR_DEVICE\ndevice-001\ndesktop-agent-001",
            identity.signature_payload().expect("valid payload").as_slice()
        );
    }

    #[test]
    fn exposes_all_desktop_unpairing_screen_states_tests() {
        let identity = valid_identity_tests();
        let states = [
            DesktopUnpairingController::confirmation_required(&identity),
            DesktopUnpairingController::requested(),
            DesktopUnpairingController::unpaired(),
            DesktopUnpairingController::api_failure("Falha ao comunicar API"),
            DesktopUnpairingController::sync_pending(),
        ];

        assert_eq!(DesktopUnpairingStage::ConfirmationRequired, states[0].stage);
        assert!(states[0].detail.contains("Notebook operacional"));
        assert_eq!(DesktopUnpairingStage::Requested, states[1].stage);
        assert!(!states[1].destructive_action_enabled);
        assert_eq!(DesktopUnpairingStage::Unpaired, states[2].stage);
        assert_eq!(DesktopUnpairingStage::ApiFailure, states[3].stage);
        assert_eq!("Falha ao comunicar API", states[3].detail);
        assert_eq!(DesktopUnpairingStage::SyncPending, states[4].stage);
    }

    #[test]
    fn rejects_incomplete_desktop_pairing_identity_tests() {
        let mut identity = valid_identity_tests();
        identity.device_id = " ".to_owned();
        assert_eq!(DesktopUnpairingError::MissingDeviceId, identity.validate().unwrap_err());

        identity = valid_identity_tests();
        identity.device_name = "".to_owned();
        assert_eq!(DesktopUnpairingError::MissingDeviceName, identity.validate().unwrap_err());

        identity = valid_identity_tests();
        identity.instance_url = "simpleguard.local".to_owned();
        assert_eq!(DesktopUnpairingError::InvalidInstanceUrl, identity.validate().unwrap_err());

        identity = valid_identity_tests();
        identity.agent_instance_id = "".to_owned();
        assert_eq!(
            DesktopUnpairingError::MissingAgentInstanceId,
            identity.validate().unwrap_err()
        );
    }

    fn valid_identity_tests() -> DesktopPairingIdentity {
        DesktopPairingIdentity {
            device_id: " device-001 ".to_owned(),
            device_name: "Notebook operacional".to_owned(),
            instance_url: " https://simpleguard.local/ ".to_owned(),
            agent_instance_id: " desktop-agent-001 ".to_owned(),
        }
    }
}
