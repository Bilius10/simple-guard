use crate::pairing::{
    DesktopAgentIdentity, DesktopAgentKey, DesktopPairingConfig, DesktopPairingRequest,
    DesktopPlatform, PairingContractError,
};
use crate::unpairing::{
    DesktopPairingIdentity, DesktopUnpairingController, DesktopUnpairingViewState,
};

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum DesktopPairingStage {
    WaitingCode,
    Connecting,
    Validating,
    Paired,
    Failure,
}

impl DesktopPairingStage {
    pub fn badge(self) -> &'static str {
        match self {
            Self::WaitingCode => "AGUARDANDO CODIGO",
            Self::Connecting => "CONECTANDO",
            Self::Validating => "VALIDANDO",
            Self::Paired => "PAREADO",
            Self::Failure => "FALHA",
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopSystemContext {
    pub platform: DesktopPlatform,
    pub computer_name: String,
    pub user_name: String,
    pub os_label: String,
}

impl DesktopSystemContext {
    pub fn detect() -> Self {
        Self {
            platform: detect_platform(),
            computer_name: first_present_env(&["COMPUTERNAME", "HOSTNAME"])
                .unwrap_or_else(|| "computador-nao-identificado".to_owned()),
            user_name: first_present_env(&["USERNAME", "USER"])
                .unwrap_or_else(|| "usuario-nao-identificado".to_owned()),
            os_label: std::env::consts::OS.to_owned(),
        }
    }

    pub fn user_os_label(&self) -> String {
        format!("{} / {}", self.user_name, self.os_label)
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopPairingInput {
    pub instance_url: String,
    pub pairing_code: String,
    pub public_key: String,
    pub agent_instance_id: Option<String>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopPairingViewState {
    pub stage: DesktopPairingStage,
    pub title: &'static str,
    pub instance_url: String,
    pub pairing_code: String,
    pub computer_name: String,
    pub user_os: String,
    pub validation_state: String,
    pub action_label: &'static str,
    pub failure: Option<String>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopWelcomeViewState {
    pub title: &'static str,
    pub detail: &'static str,
    pub primary_action: &'static str,
    pub secondary_action: &'static str,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DesktopUnpairingScreenState {
    pub title: &'static str,
    pub instance_url: String,
    pub device_name: String,
    pub warning: String,
    pub result: DesktopUnpairingViewState,
    pub action_label: &'static str,
}

pub struct DesktopAgentUiController;

impl DesktopAgentUiController {
    pub fn welcome() -> DesktopWelcomeViewState {
        DesktopWelcomeViewState {
            title: "Agente SimpleGuard",
            detail: "Este agente conecta este computador a sua instancia SimpleGuard.",
            primary_action: "Iniciar pareamento",
            secondary_action: "Configurar manualmente",
        }
    }

    pub fn pairing_waiting(context: &DesktopSystemContext) -> DesktopPairingViewState {
        pairing_state(
            DesktopPairingStage::WaitingCode,
            "",
            "",
            context,
            "Informe URL da instancia e codigo de pareamento.",
            "Validar pareamento",
            None,
        )
    }

    pub fn pairing_connecting(
        input: &DesktopPairingInput,
        context: &DesktopSystemContext,
    ) -> DesktopPairingViewState {
        pairing_state(
            DesktopPairingStage::Connecting,
            &input.instance_url,
            &input.pairing_code,
            context,
            "Conectando com a instancia.",
            "Conectando...",
            None,
        )
    }

    pub fn pairing_validating(
        input: &DesktopPairingInput,
        context: &DesktopSystemContext,
    ) -> DesktopPairingViewState {
        match build_pairing_request(input, context) {
            Ok(_) => pairing_state(
                DesktopPairingStage::Validating,
                &input.instance_url,
                &input.pairing_code,
                context,
                "Contrato local validado; pronto para enviar a API.",
                "Enviar pareamento",
                None,
            ),
            Err(error) => Self::pairing_failure(input, context, error.to_string()),
        }
    }

    pub fn pairing_paired(
        input: &DesktopPairingInput,
        context: &DesktopSystemContext,
        device_name: impl Into<String>,
    ) -> DesktopPairingViewState {
        pairing_state(
            DesktopPairingStage::Paired,
            &input.instance_url,
            &input.pairing_code,
            context,
            &format!("Computador pareado como {}.", device_name.into()),
            "Abrir status",
            None,
        )
    }

    pub fn pairing_failure(
        input: &DesktopPairingInput,
        context: &DesktopSystemContext,
        message: impl Into<String>,
    ) -> DesktopPairingViewState {
        let failure = message.into();
        pairing_state(
            DesktopPairingStage::Failure,
            &input.instance_url,
            &input.pairing_code,
            context,
            &failure.clone(),
            "Tentar novamente",
            Some(failure),
        )
    }

    pub fn unpairing_confirmation(
        identity: &DesktopPairingIdentity,
    ) -> DesktopUnpairingScreenState {
        DesktopUnpairingScreenState {
            title: "Despareamento",
            instance_url: identity.instance_url.trim().to_owned(),
            device_name: identity.device_name.trim().to_owned(),
            warning:
                "Esta acao solicita revogacao das credenciais e interrompe telemetria e comandos."
                    .to_owned(),
            result: DesktopUnpairingController::confirmation_required(identity),
            action_label: "Desparear",
        }
    }

    pub fn unpairing_requested(identity: &DesktopPairingIdentity) -> DesktopUnpairingScreenState {
        unpairing_screen(
            identity,
            DesktopUnpairingController::requested(),
            "Tentar sincronizar",
        )
    }

    pub fn unpairing_unpaired(identity: &DesktopPairingIdentity) -> DesktopUnpairingScreenState {
        unpairing_screen(
            identity,
            DesktopUnpairingController::unpaired(),
            "Voltar ao pareamento",
        )
    }

    pub fn unpairing_failure(
        identity: &DesktopPairingIdentity,
        message: impl Into<String>,
    ) -> DesktopUnpairingScreenState {
        unpairing_screen(
            identity,
            DesktopUnpairingController::api_failure(message),
            "Tentar novamente",
        )
    }
}

pub fn build_pairing_request(
    input: &DesktopPairingInput,
    context: &DesktopSystemContext,
) -> Result<DesktopPairingRequest, PairingContractError> {
    let identity = match &input.agent_instance_id {
        Some(value) => DesktopAgentIdentity::from_agent_instance_id(value)?,
        None => DesktopAgentIdentity::from_local_seed(context.platform, &context.computer_name)?,
    };

    DesktopPairingConfig {
        instance_url: input.instance_url.clone(),
        pairing_code: input.pairing_code.clone(),
        platform: context.platform,
        identity,
        key: DesktopAgentKey::from_public_key(input.public_key.clone())?,
    }
    .complete_pairing_request()
}

pub fn render_welcome(state: &DesktopWelcomeViewState) -> String {
    render_window(
        "Desktop Agent / 01 - Boas-vindas",
        &[
            ("Titulo", state.title.to_owned()),
            ("Resumo", state.detail.to_owned()),
            ("Acao primaria", state.primary_action.to_owned()),
            ("Acao secundaria", state.secondary_action.to_owned()),
        ],
        "Pronto para iniciar pareamento",
    )
}

pub fn render_pairing(state: &DesktopPairingViewState) -> String {
    let mut rows = vec![
        ("Estado", state.stage.badge().to_owned()),
        ("URL da instancia", empty_marker(&state.instance_url)),
        ("Codigo de pareamento", empty_marker(&state.pairing_code)),
        ("Nome do computador", state.computer_name.clone()),
        ("Usuario/SO detectado", state.user_os.clone()),
        ("Validacao", state.validation_state.clone()),
        ("Acao", state.action_label.to_owned()),
    ];

    if let Some(failure) = &state.failure {
        rows.push(("Falha", failure.clone()));
    }

    render_window(state.title, &rows, state.stage.badge())
}

pub fn render_unpairing(state: &DesktopUnpairingScreenState) -> String {
    render_window(
        "Desktop Agent / 08 - Despareamento",
        &[
            ("Titulo", state.title.to_owned()),
            ("Instancia atual", state.instance_url.clone()),
            ("Nome do dispositivo", state.device_name.clone()),
            ("Aviso de impacto", state.warning.clone()),
            ("Estado", state.result.badge.to_owned()),
            ("Resultado", state.result.detail.clone()),
            ("Acao", state.action_label.to_owned()),
        ],
        state.result.badge,
    )
}

fn pairing_state(
    stage: DesktopPairingStage,
    instance_url: &str,
    pairing_code: &str,
    context: &DesktopSystemContext,
    validation_state: &str,
    action_label: &'static str,
    failure: Option<String>,
) -> DesktopPairingViewState {
    DesktopPairingViewState {
        stage,
        title: "Desktop Agent / 02 - Pareamento",
        instance_url: instance_url.trim().to_owned(),
        pairing_code: pairing_code.trim().to_owned(),
        computer_name: context.computer_name.clone(),
        user_os: context.user_os_label(),
        validation_state: validation_state.to_owned(),
        action_label,
        failure,
    }
}

fn unpairing_screen(
    identity: &DesktopPairingIdentity,
    result: DesktopUnpairingViewState,
    action_label: &'static str,
) -> DesktopUnpairingScreenState {
    DesktopUnpairingScreenState {
        title: "Despareamento",
        instance_url: identity.instance_url.trim().to_owned(),
        device_name: identity.device_name.trim().to_owned(),
        warning: "Esta acao solicita revogacao das credenciais e interrompe telemetria e comandos."
            .to_owned(),
        result,
        action_label,
    }
}

fn render_window(title: &str, rows: &[(&str, String)], footer: &str) -> String {
    let mut output = String::new();
    output.push_str("+--------------------------------------------------+\n");
    output.push_str(&format!("| SimpleGuard Agent :: {} |\n", title));
    output.push_str("+--------------------------------------------------+\n");
    for (label, value) in rows {
        output.push_str(&format!("{:<22} {}\n", label, value));
    }
    output.push_str("+--------------------------------------------------+\n");
    output.push_str(footer);
    output
}

fn empty_marker(value: &str) -> String {
    if value.trim().is_empty() {
        "Nao informado".to_owned()
    } else {
        value.trim().to_owned()
    }
}

fn first_present_env(keys: &[&str]) -> Option<String> {
    keys.iter()
        .filter_map(|key| std::env::var(key).ok())
        .map(|value| value.trim().to_owned())
        .find(|value| !value.is_empty())
}

fn detect_platform() -> DesktopPlatform {
    match std::env::consts::OS {
        "windows" => DesktopPlatform::Windows,
        "macos" => DesktopPlatform::Macos,
        _ => DesktopPlatform::Linux,
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::unpairing::DesktopUnpairingStage;

    #[test]
    fn renders_desktop_welcome_screen_tests() {
        let output = render_welcome(&DesktopAgentUiController::welcome());

        assert!(output.contains("Desktop Agent / 01 - Boas-vindas"));
        assert!(output.contains("Agente SimpleGuard"));
        assert!(output.contains("Iniciar pareamento"));
        assert!(output.contains("Configurar manualmente"));
    }

    #[test]
    fn renders_pairing_fields_and_waiting_state_tests() {
        let context = context_tests();
        let state = DesktopAgentUiController::pairing_waiting(&context);
        let output = render_pairing(&state);

        assert_eq!(DesktopPairingStage::WaitingCode, state.stage);
        assert!(output.contains("AGUARDANDO CODIGO"));
        assert!(output.contains("URL da instancia"));
        assert!(output.contains("Codigo de pareamento"));
        assert!(output.contains("Notebook-Joao"));
        assert!(output.contains("joao / windows"));
    }

    #[test]
    fn validates_pairing_input_using_core_contract_tests() {
        let context = context_tests();
        let input = valid_pairing_input_tests();

        let state = DesktopAgentUiController::pairing_validating(&input, &context);
        let request = build_pairing_request(&input, &context).expect("valid request");

        assert_eq!(DesktopPairingStage::Validating, state.stage);
        assert_eq!("ABCD-2345", request.pairing_code);
        assert_eq!("desktop-windows-notebook-joao", request.agent_instance_id);
        assert_eq!(DesktopPlatform::Windows, request.platform);
    }

    #[test]
    fn renders_pairing_failure_when_input_is_invalid_tests() {
        let context = context_tests();
        let mut input = valid_pairing_input_tests();
        input.instance_url = "simpleguard.local".to_owned();

        let state = DesktopAgentUiController::pairing_validating(&input, &context);
        let output = render_pairing(&state);

        assert_eq!(DesktopPairingStage::Failure, state.stage);
        assert!(output.contains("FALHA"));
        assert!(output.contains("instance URL must start with http:// or https://"));
    }

    #[test]
    fn renders_unpairing_confirmation_screen_with_api_contract_tests() {
        let identity = valid_unpairing_identity_tests();

        let state = DesktopAgentUiController::unpairing_confirmation(&identity);
        let output = render_unpairing(&state);

        assert_eq!(
            DesktopUnpairingStage::ConfirmationRequired,
            state.result.stage
        );
        assert!(output.contains("Desktop Agent / 08 - Despareamento"));
        assert!(output.contains("https://simpleguard.local"));
        assert!(output.contains("Notebook-Joao"));
        assert_eq!(
            "https://simpleguard.local/api/agent/devices/device-001/pairing",
            identity.endpoint().expect("valid endpoint")
        );
    }

    #[test]
    fn renders_unpairing_requested_unpaired_and_failure_states_tests() {
        let identity = valid_unpairing_identity_tests();

        let requested = DesktopAgentUiController::unpairing_requested(&identity);
        let unpaired = DesktopAgentUiController::unpairing_unpaired(&identity);
        let failure = DesktopAgentUiController::unpairing_failure(&identity, "Falha API");

        assert_eq!(DesktopUnpairingStage::Requested, requested.result.stage);
        assert_eq!(DesktopUnpairingStage::Unpaired, unpaired.result.stage);
        assert_eq!(DesktopUnpairingStage::ApiFailure, failure.result.stage);
        assert!(render_unpairing(&failure).contains("Falha API"));
    }

    fn context_tests() -> DesktopSystemContext {
        DesktopSystemContext {
            platform: DesktopPlatform::Windows,
            computer_name: "Notebook-Joao".to_owned(),
            user_name: "joao".to_owned(),
            os_label: "windows".to_owned(),
        }
    }

    fn valid_pairing_input_tests() -> DesktopPairingInput {
        DesktopPairingInput {
            instance_url: "https://simpleguard.local".to_owned(),
            pairing_code: "ABCD-2345".to_owned(),
            public_key: format!("desktop-public-key-{}", "A".repeat(80)),
            agent_instance_id: None,
        }
    }

    fn valid_unpairing_identity_tests() -> DesktopPairingIdentity {
        DesktopPairingIdentity {
            device_id: "device-001".to_owned(),
            device_name: "Notebook-Joao".to_owned(),
            instance_url: "https://simpleguard.local".to_owned(),
            agent_instance_id: "desktop-windows-notebook-joao".to_owned(),
        }
    }
}
