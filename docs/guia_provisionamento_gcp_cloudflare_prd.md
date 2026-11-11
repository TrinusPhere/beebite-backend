# Guia Passo a Passo de Infraestrutura em Produção — Google Cloud + Cloudflare + Edge (BiteFlow)

Este guia prático traz **todos os comandos prontos (copiar e colar)** para você provisionar a infraestrutura de produção do **BiteFlow** seguindo a arquitetura **FinOps Estágio 1 (~R\$ 95 a R\$ 165/mês)** e **DevSecOps** para o modelo híbrido (**Opção A: Nuvem + Servidor Local Edge**).

> **Dica:** Você não precisa instalar nada no seu computador para executar os comandos do Google Cloud. Basta abrir o [Google Cloud Console](https://console.cloud.google.com/) e clicar no ícone do **Cloud Shell (`>_`)** no canto superior direito da tela.

---

## Passo 1 — Criar o Projeto no Google Cloud e Ativar APIs

No **Cloud Shell** do Google Cloud, defina o ID do projeto de produção e ative apenas as APIs necessárias:

```bash
# 1. Defina o ID do seu projeto de produção
export GCP_PROJECT_ID="biteflow-prd"
export GCP_REGION="southamerica-east1"
export GCP_ZONE="southamerica-east1-a"

# 2. Crie o projeto (caso ainda não tenha criado pelo painel)
gcloud projects create $GCP_PROJECT_ID --name="BiteFlow Production"
gcloud config set project $GCP_PROJECT_ID

# 3. Vincule sua conta de faturamento (liste o ID e vincule)
gcloud billing accounts list
# Substitua SEU_BILLING_ACCOUNT_ID pelo ID listado acima:
# gcloud billing projects link $GCP_PROJECT_ID --billing-account=SEU_BILLING_ACCOUNT_ID

# 4. Ative as APIs essenciais (Compute Engine, Pub/Sub e Artifact Registry)
gcloud services enable \
  compute.googleapis.com \
  pubsub.googleapis.com \
  artifactregistry.googleapis.com \
  iam.googleapis.com
```

---

## Passo 2 — Configurar o Alerta de Orçamento FinOps (Obrigatório)

Antes de subir qualquer máquina, trave seu teto financeiro para nunca ter surpresas na fatura:

1. No menu do Google Cloud, acesse **Faturamento (Billing) → Orçamentos e alertas (Budgets & alerts)**.
2. Clique em **Criar orçamento (Create Budget)**:
   - **Nome:** `Alerta Mensal BiteFlow PRD`
   - **Valor alvo:** `R$ 180,00` (ou `US$ 30.00`)
   - **Limites de alerta:** `50%`, `80%` e `100%` (marque para enviar e-mail).

---

## Passo 3 — Criar os 9 Tópicos e 7 Subscriptions do Cloud Pub/Sub (Custo R\$ 0/mês)

O Google Cloud oferece **10 GB/mês gratuitos** no Pub/Sub. Os nomes abaixo são **exatamente** os tópicos e subscriptions que o `bite-flow` e o `biteflow-ifood-service` consomem no código.

Copie e cole este bloco inteiro no **Cloud Shell**:

```bash
# ── 1. Criar os 9 Tópicos Oficiais do BiteFlow ──────────────────────────────
for TOPIC in \
  ifood-order-placed \
  ifood-order-status-changed \
  ifood-chat-message \
  ifood-order-dispute \
  ifood-order-cancellation-requested \
  biteflow-order-status-changed \
  biteflow-order-cancelled \
  biteflow-catalog-changed \
  biteflow-chat-message-outbound; do
  gcloud pubsub topics create "$TOPIC" --project="$GCP_PROJECT_ID" || true
done

# ── 2. Criar as 7 Subscriptions com Ack Deadline de 60s ────────────────────
gcloud pubsub subscriptions create ifood-order-placed-sub \
  --topic=ifood-order-placed --ack-deadline=60 --project="$GCP_PROJECT_ID"

gcloud pubsub subscriptions create ifood-order-status-changed-sub \
  --topic=ifood-order-status-changed --ack-deadline=60 --project="$GCP_PROJECT_ID"

gcloud pubsub subscriptions create ifood-chat-message-sub \
  --topic=ifood-chat-message --ack-deadline=60 --project="$GCP_PROJECT_ID"

gcloud pubsub subscriptions create biteflow-order-status-changed-sub \
  --topic=biteflow-order-status-changed --ack-deadline=60 --project="$GCP_PROJECT_ID"

gcloud pubsub subscriptions create biteflow-order-cancelled-sub \
  --topic=biteflow-order-cancelled --ack-deadline=60 --project="$GCP_PROJECT_ID"

gcloud pubsub subscriptions create biteflow-catalog-changed-sub \
  --topic=biteflow-catalog-changed --ack-deadline=60 --project="$GCP_PROJECT_ID"

gcloud pubsub subscriptions create biteflow-chat-message-outbound-sub \
  --topic=biteflow-chat-message-outbound --ack-deadline=60 --project="$GCP_PROJECT_ID"
```

---

## Passo 4 — Criar Service Account com Menor Privilégio (IAM DevSecOps)

Em vez de gerar arquivos `.json` de credenciais (que podem vazar), criamos uma identidade de serviço que a própria VM assume automaticamente:

```bash
# 1. Criar a Service Account de runtime
gcloud iam service-accounts create biteflow-runtime-sa \
  --display-name="BiteFlow Production Runtime SA" \
  --project="$GCP_PROJECT_ID"

SA_EMAIL="biteflow-runtime-sa@${GCP_PROJECT_ID}.iam.gserviceaccount.com"

# 2. Conceder APENAS permissão de publicar e consumir no Pub/Sub + ler imagens no Artifact Registry
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/pubsub.publisher"

gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/pubsub.subscriber"

gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/artifactregistry.reader"
```

---

## Passo 5 — Criar IP Fixo, Firewall Seguro e a VM em São Paulo (`southamerica-east1`)

> **Segurança DevSecOps:** As portas `5432` (PostgreSQL), `8080` (`bite-flow`) e `8081` (`biteflow-ifood-service`) **NÃO** serão abertas na internet! Apenas as portas `80` e `443` (HTTPS gerenciado pelo proxy reverso **Caddy**) ficam abertas.

```bash
# 1. Reservar um IP Estático Regional em São Paulo
gcloud compute addresses create biteflow-cloud-ip \
  --region="$GCP_REGION" \
  --project="$GCP_PROJECT_ID"

STATIC_IP=$(gcloud compute addresses describe biteflow-cloud-ip \
  --region="$GCP_REGION" --project="$GCP_PROJECT_ID" --format="get(address)")
echo "Guarde este IP para configurar no DNS da Cloudflare: $STATIC_IP"

# 2. Criar regra de Firewall liberando APENAS HTTP (80) e HTTPS (443)
gcloud compute firewall-rules create allow-biteflow-https \
  --allow=tcp:80,tcp:443 \
  --target-tags=biteflow-web \
  --description="Permite trafego HTTP/HTTPS para o proxy reverso Caddy" \
  --project="$GCP_PROJECT_ID"

# 3. Criar a VM Ubuntu 24.04 LTS em São Paulo (e2-medium: 2 vCPU, 4GB RAM, 30GB SSD Balanceado)
gcloud compute instances create biteflow-cloud-vm \
  --project="$GCP_PROJECT_ID" \
  --zone="$GCP_ZONE" \
  --machine-type=e2-medium \
  --address="$STATIC_IP" \
  --service-account="$SA_EMAIL" \
  --scopes=https://www.googleapis.com/auth/cloud-platform \
  --tags=biteflow-web \
  --image-family=ubuntu-2404-lts-amd64 \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --boot-disk-type=pd-balanced
```

---

## Passo 6 — Preparar a VM e Gerar os Segredos Exclusivos de Produção

Conecte-se à VM pelo Cloud Shell:

```bash
gcloud compute ssh biteflow-cloud-vm --zone="$GCP_ZONE" --project="$GCP_PROJECT_ID"
```

Dentro da VM, instale o Docker, ative 2 GB de Swap (proteção extra contra picos de memória) e gere as chaves criptográficas exclusivas de produção:

```bash
# 1. Instalar Docker e Docker Compose Plugin
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER

# 2. Criar 2GB de Swap por segurança operacional
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 3. Criar estrutura de diretórios do BiteFlow em /opt/biteflow
sudo mkdir -p /opt/biteflow/{secrets,init-db,uploads/images,backups}
sudo chown -R $USER:$USER /opt/biteflow
cd /opt/biteflow

# 4. Gerar par de chaves RSA 2048 EXCLUSIVO de produção para assinar o JWT
openssl genpkey -algorithm RSA -out /opt/biteflow/secrets/privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in /opt/biteflow/secrets/privateKey.pem -out /opt/biteflow/secrets/publicKey.pem
chmod 600 /opt/biteflow/secrets/privateKey.pem
chmod 644 /opt/biteflow/secrets/publicKey.pem

# 5. Gerar chave AES-256 de 32 caracteres aleatórios para o iFood e senha do Postgres
echo "IFOOD_ENCRYPTION_KEY sugerida: $(openssl rand -base64 32 | head -c 32)"
echo "DB_PASSWORD sugerida: $(openssl rand -hex 16)"
```

---

## Passo 7 — Configurar o Docker Compose de Produção (1 Postgres com 2 Databases + Caddy TLS)

Ainda dentro de `/opt/biteflow` na VM, crie os 4 arquivos abaixo:

### 7.1. `/opt/biteflow/init-db/01-create-ifood-db.sql`
Cria os dois bancos (`biteflow` e `biteflow_ifood`) dentro do **mesmo** container PostgreSQL 17 para economizar RAM:

```sql
CREATE DATABASE biteflow_ifood;
GRANT ALL PRIVILEGES ON DATABASE biteflow_ifood TO postgres;
```

### 7.2. `/opt/biteflow/.env`
Preencha com os valores gerados no Passo 6:

```dotenv
GCP_PROJECT_ID=biteflow-prd
DB_USER=postgres
DB_PASSWORD=COLE_AQUI_SUA_SENHA_FORTE_DO_BANCO
BITEFLOW_INITIAL_ADMIN_PASSWORD=COLE_AQUI_SUA_SENHA_FORTE_DO_ADMIN
IFOOD_ENCRYPTION_KEY=COLE_AQUI_OS_32_CARACTERES_GERADOS
API_DOMAIN=api.seudominio.com.br
FRONTEND_ORIGIN=https://app.seudominio.com.br
```

Proteja o arquivo `.env`:
```bash
chmod 600 /opt/biteflow/.env
```

### 7.3. `/opt/biteflow/Caddyfile`
O Caddy emite e renova sozinho o certificado HTTPS (Let's Encrypt) e faz o roteamento para os dois microsserviços:

```caddyfile
{$API_DOMAIN} {
    encode zstd gzip

    # Rotas do microsserviço iFood (credenciais, catálogo sync, status, disputas)
    @ifood_service {
        path /api/v1/ifood/* /api/images/*
    }
    handle @ifood_service {
        reverse_proxy biteflow-ifood:8081
    }

    # Todas as demais rotas vão para o backend principal BiteFlow
    handle {
        reverse_proxy biteflow-api:8080
    }
}
```

### 7.4. `/opt/biteflow/docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:17-alpine
    container_name: biteflow-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: biteflow
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./init-db:/docker-entrypoint-initdb.d:ro
    command: >
      postgres -c shared_buffers=256MB -c max_connections=100
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d biteflow"]
      interval: 10s
      timeout: 5s
      retries: 5

  biteflow-api:
    image: ghcr.io/SEU_USUARIO_GITHUB/bite-flow:latest
    container_name: biteflow-api
    restart: unless-stopped
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      QUARKUS_PROFILE: prod
      DB_HOST: postgres
      DB_NAME: biteflow
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      GCP_PROJECT_ID: ${GCP_PROJECT_ID}
      JWT_PRIVATE_KEY_LOCATION: file:/run/secrets/privateKey.pem
      JWT_PUBLIC_KEY_LOCATION: file:/run/secrets/publicKey.pem
      BITEFLOW_COOKIE_SECURE: "true"
      BITEFLOW_COOKIE_SAME_SITE: Lax
      BITEFLOW_ENFORCE_PROD_GUARDS: "true"
      BITEFLOW_INITIAL_ADMIN_PASSWORD: ${BITEFLOW_INITIAL_ADMIN_PASSWORD}
      JAVA_OPTS_APPEND: "-XX:MaxRAMPercentage=75.0"
    volumes:
      - ./secrets/privateKey.pem:/run/secrets/privateKey.pem:ro
      - ./secrets/publicKey.pem:/run/secrets/publicKey.pem:ro
    deploy:
      resources:
        limits:
          memory: 768M

  biteflow-ifood:
    image: ghcr.io/SEU_USUARIO_GITHUB/biteflow-ifood-service:latest
    container_name: biteflow-ifood
    restart: unless-stopped
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      QUARKUS_PROFILE: prod
      DB_HOST: postgres
      DB_NAME: biteflow_ifood
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      GCP_PROJECT_ID: ${GCP_PROJECT_ID}
      IFOOD_ENCRYPTION_KEY: ${IFOOD_ENCRYPTION_KEY}
      BITEFLOW_ENFORCE_PROD_KEY: "true"
      BITEFLOW_IMAGE_STORAGE_DIR: /var/lib/biteflow/images
      BITEFLOW_IMAGE_PUBLIC_BASE_URL: https://${API_DOMAIN}/api/images
      JAVA_OPTS_APPEND: "-XX:MaxRAMPercentage=75.0"
    volumes:
      - ./uploads/images:/var/lib/biteflow/images
    deploy:
      resources:
        limits:
          memory: 512M

  caddy:
    image: caddy:2-alpine
    container_name: biteflow-caddy
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    environment:
      API_DOMAIN: ${API_DOMAIN}
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy_data:/data
      - caddy_config:/config
    depends_on:
      - biteflow-api
      - biteflow-ifood

volumes:
  pgdata:
  caddy_data:
  caddy_config:
```

---

## Passo 8 — Configurar DNS e Hospedar o `biteflow-front` Grátis na Cloudflare

1. **Apontar o DNS da API:**
   - No painel da Cloudflare do seu domínio, crie um registro tipo `A`:
     - **Nome:** `api`
     - **Endereço IPv4:** `<STATIC_IP_DA_VM_DO_PASSO_5>`
     - **Proxy status:** Nuvem cinza (DNS Only) no primeiro minuto para o Caddy emitir o certificado Let's Encrypt; depois pode ativar a nuvem laranja com SSL em modo **Full (Strict)**.
2. **Publicar o `biteflow-front` no Cloudflare Pages (Custo R\$ 0/mês):**
   - No menu da Cloudflare, acesse **Workers & Pages → Create → Pages → Connect to Git**.
   - Selecione seu repositório, defina:
     - **Root directory:** `biteflow-front`
     - **Build command:** `npm run build`
     - **Build output directory:** `dist`
     - **Environment variable:** `VITE_API_URL=https://api.seudominio.com.br`
   - Adicione o domínio customizado `app.seudominio.com.br` (dica: mantendo `app.` e `api.` sob o mesmo domínio raiz `.seudominio.com.br`, o cookie `biteflow_jwt` com `SameSite=Lax` funciona perfeitamente em qualquer celular Safari/iOS e Android!).

---

## Passo 9 — Preparar o Mini-PC Local da Unidade Balcão (Modo `EDGE` + Tailscale + Auto-Update)

No Mini-PC (Ubuntu Server LTS) que ficará fisicamente na loja de Balcão:

```bash
# 1. Instalar Docker e Tailscale (VPN Mesh Zero-Trust)
curl -fsSL https://get.docker.com | sudo sh
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up --ssh

# 2. Garantir sincronismo de relógio (NTP)
sudo apt-get update && sudo apt-get install -y chrony
sudo systemctl enable --now chrony
```

No `docker-compose.edge.yml` do Mini-PC local:
- O `bite-flow` roda com `BITEFLOW_SYNC_MODE=EDGE`, `BITEFLOW_IFOOD_ENABLED=false`, `BITEFLOW_COOKIE_SECURE=false` (para acesso rápido via HTTP na LAN local `http://192.168.1.10`) e `BITEFLOW_CLOUD_SYNC_URL=https://api.seudominio.com.br`.
- Adicione o container **Watchtower** agendado para **04:00 da manhã**:

```yaml
  watchtower:
    image: containrrr/watchtower:latest
    container_name: biteflow-auto-updater
    restart: unless-stopped
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      TZ: America/Sao_Paulo
      WATCHTOWER_CLEANUP: "true"
      WATCHTOWER_SCHEDULE: "0 0 4 * * *"
```

### E para a Impressora Térmica da Unidade Delivery (que roda na Nuvem):
No computador da expedição do Delivery, instale o **Tailscale** e exponha o IP local da impressora térmica (ex.: `192.168.1.50/32`) para a VM da Nuvem:
```bash
sudo tailscale up --advertise-routes=192.168.1.50/32
```
Assim, quando um pedido do iFood chegar na VM da nuvem, o `TcpPrinterGateway` consegue abrir socket na porta `9100` da impressora da cozinha do Delivery através do túnel criptografado!

