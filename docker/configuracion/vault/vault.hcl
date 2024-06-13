storage "file" {
  path = "/mnt/vault/data"
}
listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_cert_file = "/configuracion/vault/fullchain.pem"
  tls_key_file  = "/configuracion/vault/privkey1.pem"
}

api_addr = "https://127.0.0.1:8200"
cluster_addr = "https://127.0.0.1:8201"
ui = true
disable_mlock = true