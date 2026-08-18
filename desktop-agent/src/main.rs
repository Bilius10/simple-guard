use serde::{Deserialize, Serialize};
use simpleguard_desktop_agent::api::{extract_json_string_field, DesktopAgentApiClient};
use simpleguard_desktop_agent::key_store::DesktopAgentKeyPair;
use simpleguard_desktop_agent::local_store::{DesktopLocalPairing, DesktopLocalStore};
use simpleguard_desktop_agent::ui::{
    build_pairing_request, DesktopPairingInput, DesktopSystemContext,
};
use simpleguard_desktop_agent::unpairing::DesktopPairingIdentity;

#[derive(Debug, Clone, Serialize)]
struct AgentStatusResponse {
    has_pairing: bool,
    instance_url: String,
    device_id: String,
    device_name: String,
    agent_instance_id: String,
    computer_name: String,
    user_os: String,
    platform: String,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
struct PairingRequestDto {
    instance_url: String,
    pairing_code: String,
}

#[derive(Debug, Clone, Serialize)]
struct PairingResponseDto {
    device_id: String,
    device_name: String,
    agent_instance_id: String,
    pairing_status: String,
}

#[derive(Debug, Clone, Serialize)]
struct UnpairingResponseDto {
    request_id: String,
    status: String,
}

#[derive(Debug, Clone, Serialize)]
struct PairingSyncResponseDto {
    pairing_status: String,
    unpairing_status: String,
}

#[tauri::command]
fn agent_status() -> Result<AgentStatusResponse, String> {
    let context = DesktopSystemContext::detect();
    let local = DesktopLocalStore::load().map_err(|error| error.to_string())?;
    let user_os = context.user_os_label();
    let platform = context.platform.api_value().to_owned();

    Ok(match local {
        Some(pairing) => AgentStatusResponse {
            has_pairing: true,
            instance_url: pairing.instance_url,
            device_id: pairing.device_id,
            device_name: pairing.device_name,
            agent_instance_id: pairing.agent_instance_id,
            computer_name: context.computer_name,
            user_os,
            platform,
        },
        None => AgentStatusResponse {
            has_pairing: false,
            instance_url: String::new(),
            device_id: String::new(),
            device_name: context.computer_name.clone(),
            agent_instance_id: String::new(),
            computer_name: context.computer_name,
            user_os,
            platform,
        },
    })
}

#[tauri::command]
fn complete_pairing(request: PairingRequestDto) -> Result<PairingResponseDto, String> {
    let context = DesktopSystemContext::detect();
    let key_pair = DesktopAgentKeyPair::generate().map_err(|error| error.to_string())?;
    let input = DesktopPairingInput {
        instance_url: request.instance_url.trim().to_owned(),
        pairing_code: request.pairing_code.trim().to_owned(),
        public_key: key_pair.public_key.clone(),
        agent_instance_id: None,
    };
    let pairing_request =
        build_pairing_request(&input, &context).map_err(|error| error.to_string())?;
    let response = DesktopAgentApiClient::complete_pairing(&input.instance_url, &pairing_request)
        .map_err(|error| error.to_string())?;

    let device_id = extract_json_string_field(&response.body, "deviceId")
        .ok_or_else(|| "A API nao retornou o deviceId.".to_owned())?;
    let device_name = extract_json_string_field(&response.body, "deviceName")
        .unwrap_or_else(|| context.computer_name.clone());
    let pairing_status = extract_json_string_field(&response.body, "pairingStatus")
        .unwrap_or_else(|| "paired".to_owned());

    let local = DesktopLocalPairing {
        instance_url: input.instance_url,
        device_id: device_id.clone(),
        device_name: device_name.clone(),
        agent_instance_id: pairing_request.agent_instance_id.clone(),
        public_key: key_pair.public_key,
        private_key: key_pair.private_key,
        platform: pairing_request.platform.api_value().to_owned(),
    };
    DesktopLocalStore::save(&local).map_err(|error| error.to_string())?;

    Ok(PairingResponseDto {
        device_id,
        device_name,
        agent_instance_id: pairing_request.agent_instance_id,
        pairing_status,
    })
}

#[tauri::command]
fn request_unpairing() -> Result<UnpairingResponseDto, String> {
    let pairing = DesktopLocalStore::load()
        .map_err(|error| error.to_string())?
        .ok_or_else(|| "Nenhum vinculo local encontrado.".to_owned())?;
    let signature = DesktopAgentKeyPair::sign_unpairing(
        &pairing.private_key,
        &pairing.device_id,
        &pairing.agent_instance_id,
    )
    .map_err(|error| error.to_string())?;
    let identity = DesktopPairingIdentity {
        device_id: pairing.device_id.clone(),
        device_name: pairing.device_name,
        instance_url: pairing.instance_url,
        agent_instance_id: pairing.agent_instance_id,
    };
    let response = DesktopAgentApiClient::request_unpairing(&identity, &signature)
        .map_err(|error| error.to_string())?;
    let request_id = extract_json_string_field(&response.body, "requestId").unwrap_or_default();
    let status = extract_json_string_field(&response.body, "status").unwrap_or_default();

    Ok(UnpairingResponseDto { request_id, status })
}

#[tauri::command]
fn sync_pairing_status() -> Result<PairingSyncResponseDto, String> {
    let pairing = DesktopLocalStore::load()
        .map_err(|error| error.to_string())?
        .ok_or_else(|| "Nenhum vinculo local encontrado.".to_owned())?;
    let signature = DesktopAgentKeyPair::sign_unpairing(
        &pairing.private_key,
        &pairing.device_id,
        &pairing.agent_instance_id,
    )
    .map_err(|error| error.to_string())?;
    let identity = DesktopPairingIdentity {
        device_id: pairing.device_id,
        device_name: pairing.device_name,
        instance_url: pairing.instance_url,
        agent_instance_id: pairing.agent_instance_id,
    };
    let response = DesktopAgentApiClient::pairing_status(&identity, &signature)
        .map_err(|error| error.to_string())?;
    let pairing_status = extract_json_string_field(&response.body, "pairingStatus")
        .ok_or_else(|| "A API nao retornou o estado do pareamento.".to_owned())?;
    let unpairing_status =
        extract_json_string_field(&response.body, "unpairingStatus").unwrap_or_default();

    if pairing_status == "unpaired" {
        DesktopLocalStore::delete().map_err(|error| error.to_string())?;
    }

    Ok(PairingSyncResponseDto {
        pairing_status,
        unpairing_status,
    })
}

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![
            agent_status,
            complete_pairing,
            request_unpairing,
            sync_pairing_status
        ])
        .run(tauri::generate_context!("tauri.conf.json"))
        .expect("failed to run SimpleGuard desktop agent");
}
