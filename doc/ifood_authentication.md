# iFood Authentication — Guia de Autenticação (OAuth 2.0)

Este documento contém o guia completo de autenticação da API do iFood (Merchant API), cobrindo o padrão OAuth 2.0, tipos de aplicações (Centralizadas e Distribuídas), fluxos de autorização (`client_credentials`, `authorization_code`, `refresh_token`), parâmetros de requisição e payloads.

---

## 🔑 1. Visão Geral e Protocolo

A API do iFood utiliza o protocolo **OAuth 2.0** com tokens do tipo **Bearer JWT**. Todas as requisições de negócio à API do iFood (como consulta de pedidos, catálogo, etc.) devem incluir o token no cabeçalho HTTP:

```http
Authorization: Bearer <accessToken>
```

* **URL Base de Autenticação**: `https://merchant-api.ifood.com.br`
* **Content-Type dos Endpoints de Token**: `application/x-www-form-urlencoded`

---

## 🏢 2. Tipos de Aplicação: Centralizada vs. Distribuída

O iFood classifica as integrações em dois modelos principais:

| Característica | Aplicação Centralizada (*Centralized App*) | Aplicação Distribuída (*Distributed App*) |
| :--- | :--- | :--- |
| **Infraestrutura** | Servidor único em nuvem (SaaS Multi-tenant). | Instalada na infraestrutura local do restaurante (PDVs locais). |
| **Armazenamento de Segredos** | O `clientSecret` fica seguro no servidor backend. | O `clientSecret` fica exposto no ambiente do cliente. |
| **Fluxo OAuth** | `client_credentials` | `authorization_code` com `userCode` |
| **Aprovação do Lojista** | Concessão direta via credenciais únicas. | Aprovação manual pelo Portal do Parceiro iFood. |
| **Refresh Token** | Não possui (gera novo token via `client_credentials`). | **Possui** (`refresh_token` é enviado a cada renovação). |

---

## ⚡ 3. Fluxo 1: Aplicações Centralizadas (`client_credentials`)

Utilizado quando sua aplicação roda em um servidor em nuvem centralizado e se comunica diretamente com a API do iFood.

### Requisição
* **Endpoint**: `POST /authentication/v1.0/oauth/token`
* **Content-Type**: `application/x-www-form-urlencoded`

#### Parâmetros do Corpo (Form Body):

| Campo | Tipo | Obrigatório | Descrição |
| :--- | :--- | :--- | :--- |
| `grantType` | String | Sim | Deve ser exatamente `client_credentials`. |
| `clientId` | String | Sim | Identificador único da sua aplicação no Portal do Desenvolvedor iFood. |
| `clientSecret` | String | Sim | Chave secreta da sua aplicação. |

#### Exemplo de Requisição cURL:
```bash
curl -X POST "https://merchant-api.ifood.com.br/authentication/v1.0/oauth/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grantType=client_credentials" \
  -d "clientId=SEU_CLIENT_ID" \
  -d "clientSecret=SEU_CLIENT_SECRET"
```

#### Resposta de Sucesso (HTTP 200 OK):
```json
{
  "accessToken": "eyJhbGciOiJKV1QiLCJhbGci...",
  "type": "Bearer",
  "expiresIn": 3600
}
```

---

## 🖥️ 4. Fluxo 2: Aplicações Distribuídas (`authorization_code`)

Utilizado para softwares instalados localmente no estabelecimento. Exige a autorização expressa do dono do restaurante através do Portal do Parceiro iFood.

### Diagrama da Sequência

```
  [Sua Aplicação PDV]            [iFood Auth Server]           [Portal do Lojista]
          │                               │                             │
    1.    ├─ POST /oauth/userCode ───────>│                             │
          │<─ Retorna userCode ───────────┤                             │
          │   e verificationUrl           │                             │
          │                               │                             │
    2.    │  (Exibe userCode e link no PDV)                              │
          │ ───────────────────────────────────────────────────────────>│ Lojista acessa link,
          │                                                             │ digita o userCode e
          │                                                             │ autoriza a integração.
          │<─ Lojista insere authorizationCode no PDV ──────────────────┤ Recebe authorizationCode
          │                               │                             │
    3.    ├─ POST /oauth/token ──────────>│                             │
          │  grantType: authorization_code│                             │
          │<─ Retorna accessToken ────────┤                             │
          │   e refreshToken              │                             │
```

---

### Passo 1: Solicitar o Código do Usuário (`userCode`)
O PDV solicita um código temporário para apresentar ao lojista.

* **Endpoint**: `POST /authentication/v1.0/oauth/userCode`
* **Content-Type**: `application/x-www-form-urlencoded`

#### Body:
```
clientId=SEU_CLIENT_ID
```

#### Resposta de Sucesso (HTTP 200 OK):
```json
{
  "userCode": "ABCD-1234",
  "authorizationCodeVerifier": "verifier_string_xyz789",
  "verificationUrl": "https://portal.ifood.com.br/apps/authorize",
  "verificationUrlComplete": "https://portal.ifood.com.br/apps/authorize?code=ABCD-1234",
  "expiresIn": 600
}
```
> ⚠️ **Importante**: O campo `authorizationCodeVerifier` deve ser mantido em memória no seu software pois será necessário no Passo 3.

---

### Passo 2: Autorização pelo Lojista
1. O lojista acessa a `verificationUrl` ou abre a `verificationUrlComplete`.
2. O lojista realiza o login no Portal do Parceiro iFood e digita o `userCode` (ex: `ABCD-1234`).
3. Ao autorizar, o portal gera e exibe um **`authorizationCode`**. O lojista copia este código e cola dentro da sua aplicação PDV.

---

### Passo 3: Troca do Código pelo Access Token
Com o `authorizationCode` (fornecido pelo lojista) e o `authorizationCodeVerifier` (obtido no Passo 1), o PDV solicita o token final.

* **Endpoint**: `POST /authentication/v1.0/oauth/token`
* **Content-Type**: `application/x-www-form-urlencoded`

#### Parâmetros do Corpo (Form Body):
```
grantType=authorization_code
clientId=SEU_CLIENT_ID
clientSecret=SEU_CLIENT_SECRET
authorizationCode=CODIGO_RECEBIDO_DO_LOJISTA
authorizationCodeVerifier=VERIFIER_STRING_OBTIDO_NO_PASSO_1
```

#### Resposta de Sucesso (HTTP 200 OK):
```json
{
  "accessToken": "eyJhbGciOiJKV1QiLC...",
  "type": "Bearer",
  "expiresIn": 21600,
  "refreshToken": "ref_881923812938192"
}
```

---

## 🔄 5. Fluxo 3: Renovação do Token (`refresh_token`)

Aplicações distribuídas recebem um `refreshToken` ao se autenticar. Como o `accessToken` expira após o tempo indicado em `expiresIn`, deve-se utilizar o `refreshToken` para gerar um novo token sem exigir nova autorização manual do lojista.

* **Endpoint**: `POST /authentication/v1.0/oauth/token`
* **Content-Type**: `application/x-www-form-urlencoded`

#### Parâmetros do Corpo (Form Body):
```
grantType=refresh_token
clientId=SEU_CLIENT_ID
clientSecret=SEU_CLIENT_SECRET
refreshToken=SEU_REFRESH_TOKEN_ATUAL
```

#### Resposta de Sucesso (HTTP 200 OK):
```json
{
  "accessToken": "eyJhbGciOiJKV1Q...",
  "type": "Bearer",
  "expiresIn": 21600,
  "refreshToken": "NOVO_REFRESH_TOKEN_GERADO"
}
```
> ⚠️ **Atenção**: Guarde o **novo** `refreshToken` retornado na resposta, pois o anterior pode ser invalidado (Refresh Token Rotation).

---

## 🛡️ 6. Boas Práticas e Segurança

1. **Protocolo HTTPS / TLS 1.2+**: Todas as chamadas devem utilizar HTTPS obrigatório.
2. **Armazenamento Seguro de Segredos**: Nunca expor `clientSecret` ou `refreshToken` em repositórios públicos, scripts de frontend ou código cliente não compilado.
3. **Formato das Requisições de Token**: Os endpoints de OAuth no iFood exigem o cabeçalho `Content-Type: application/x-www-form-urlencoded`. O envio de JSON nesses endpoints resultará em erro de requisição.
4. **Renovação Antecipada de Tokens**: Recomenda-se renovar o `accessToken` alguns minutos antes da sua expiração (utilizando `expiresIn`) para evitar falhas em requisições concorrentes.
5. **Tratamento de HTTP 401 Unauthorized**: Se qualquer chamada à API retornar `401 Unauthorized`, renove o token imediatamente (via `refresh_token` ou `client_credentials`) e re-tente a requisição.

---

## 🛑 7. Erros e Troubleshooting

Durante a integração, é comum encontrar alguns códigos de erro HTTP e do OAuth. Abaixo, as principais causas e soluções:

### HTTP 401 Unauthorized
* **Significado**: O Token de Acesso (`accessToken`) é inválido, está ausente ou expirou.
* **Causa Comum**: Esquecer o header `Authorization: Bearer <token>` ou enviar um token expirado.
* **Solução**: Para Aplicações Distribuídas, chame o endpoint de `/token` com `grantType=refresh_token`. Para Aplicações Centralizadas, solicite um novo token usando `grantType=client_credentials`.

### HTTP 403 Forbidden
* **Significado**: A aplicação autenticou com sucesso (token válido), mas **não tem permissão** para acessar o recurso específico ou a loja (`merchantId`).
* **Causa Comum (Aplicações Centralizadas)**: O dono do restaurante (Merchant) não aprovou a sua aplicação no Portal do Parceiro.
* **Solução**: 
  * Para Aplicações Centralizadas, você deve enviar um convite/solicitação via Portal do Desenvolvedor (aba Permissões). O lojista deve entrar no **Portal do Parceiro iFood** e aceitar o vínculo da sua aplicação antes de você poder fazer requisições.
  * Verifique se as permissões (escopos) foram habilitadas corretamente para o seu aplicativo no momento da criação.

### Erros OAuth (`invalid_grant` / `unauthorized_client`)
* **`invalid_grant`**: Ocorre quando o `authorizationCode` ou `refreshToken` já expirou ou é inválido. A solução é solicitar um novo código (`userCode`) ao usuário (no caso de Apps Distribuídos) ou garantir que está usando o `refreshToken` mais recente.
* **`unauthorized_client`**: Ocorre quando as credenciais (`clientId` e `clientSecret`) estão erradas ou o aplicativo não está liberado no ambiente chamado (ex: usar credenciais de Produção em ambiente de Sandbox).

### HTTP 429 Too Many Requests
* **Significado**: Sua aplicação excedeu o limite de requisições por segundo (Rate Limit).
* **Solução**: Não tente requisições automáticas em loop sem pausas. Implemente um **Exponential Backoff** (ex: esperar 1s, depois 2s, 4s, etc.) antes de tentar novamente.
