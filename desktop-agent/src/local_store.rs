use serde::{Deserialize, Serialize};
use std::fmt;
use std::fs;
use std::path::PathBuf;

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct DesktopLocalPairing {
    pub instance_url: String,
    pub device_id: String,
    pub device_name: String,
    pub agent_instance_id: String,
    pub public_key: String,
    pub private_key: String,
    pub platform: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum DesktopLocalStoreError {
    CurrentDirectory,
    Io(String),
    Serialization(String),
}

impl fmt::Display for DesktopLocalStoreError {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::CurrentDirectory => formatter.write_str("Falha ao localizar diretorio local"),
            Self::Io(message) => write!(formatter, "Falha de arquivo local: {}", message),
            Self::Serialization(message) => {
                write!(formatter, "Falha ao ler vinculo local: {}", message)
            }
        }
    }
}

impl std::error::Error for DesktopLocalStoreError {}

pub struct DesktopLocalStore;

impl DesktopLocalStore {
    pub fn load() -> Result<Option<DesktopLocalPairing>, DesktopLocalStoreError> {
        let path = store_path()?;
        if !path.exists() {
            return Ok(None);
        }
        let content = fs::read_to_string(path)
            .map_err(|error| DesktopLocalStoreError::Io(error.to_string()))?;
        serde_json::from_str(&content)
            .map(Some)
            .map_err(|error| DesktopLocalStoreError::Serialization(error.to_string()))
    }

    pub fn save(pairing: &DesktopLocalPairing) -> Result<(), DesktopLocalStoreError> {
        let content = serde_json::to_string_pretty(pairing)
            .map_err(|error| DesktopLocalStoreError::Serialization(error.to_string()))?;
        fs::write(store_path()?, content)
            .map_err(|error| DesktopLocalStoreError::Io(error.to_string()))
    }

    pub fn delete() -> Result<(), DesktopLocalStoreError> {
        let path = store_path()?;
        if path.exists() {
            fs::remove_file(path).map_err(|error| DesktopLocalStoreError::Io(error.to_string()))?;
        }
        Ok(())
    }
}

fn store_path() -> Result<PathBuf, DesktopLocalStoreError> {
    Ok(std::env::current_dir()
        .map_err(|_| DesktopLocalStoreError::CurrentDirectory)?
        .join("simpleguard-desktop-agent.local.json"))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn serializes_local_pairing_tests() {
        let pairing = DesktopLocalPairing {
            instance_url: "http://localhost:8080".to_owned(),
            device_id: "device-001".to_owned(),
            device_name: "Notebook".to_owned(),
            agent_instance_id: "desktop-windows-notebook".to_owned(),
            public_key: "public".to_owned(),
            private_key: "private".to_owned(),
            platform: "WINDOWS".to_owned(),
        };

        let json = serde_json::to_string(&pairing).expect("json");

        assert!(json.contains("desktop-windows-notebook"));
    }
}
