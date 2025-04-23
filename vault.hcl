storage "postgresql" {
  connection_url =  "postgresql://vault_user:Chuck1234@postgres:5432/vault?sslmode=disable"
  ha_enabled = false
}

listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_disable = "true"
}

ui = true
disable_mlock = true