# 💻  Milk Road

### Sistema desenvolvido com **Spring Boot**, **JWT Security**, **JPA (MySQL)** e integração com **Google Maps API**

---
## 📄 Índice
1. [Visão Geral](#-visão-geral)
2. [Tecnologias Utilizadas](#-tecnologias-utilizadas)
3. [Estrutura do Projeto](#-estrutura-do-projeto)
4. [Autenticação e Segurança](#-autenticação-e-segurança)
5. [Funcionalidades Principais](#-funcionalidades-principais)
6. [Cadastro de Clientes](#-cadastro-de-clientes)
7. [Regras de Entregas](#-regras-de-entregas)
8. [Integração com Google Maps](#-integração-com-google-maps)
9. [Tarefas Automáticas ](#-tarefas-automáticas)
10. [Configuração do Ambiente](#-configuração-do-ambiente)
11. [Endpoints Principais](#-endpoints-principais)
12. [Como Executar o Projeto](#-como-executar-o-projeto)
13. [Autores](#-autores)

---

## 🌐 Visão Geral

O **Milk Road** é um sistema web completo para **gestão de clientes, rotas e entregas**, com foco na otimização de trajetos diários de entregadores.

Desenvolvido com **arquitetura em camadas** (Controller, Service, Repository, Security, DTOs), segue **boas práticas de segurança e organização de código**.

Principais recursos:

- Controle de acesso por perfis (ADMIN / CLIENTE)
- Cadastro e atualização de clientes
- Gerenciamento de status (ativos/inativos)
- Geração automática de entregas (segunda a sexta)
- Rotas otimizadas via Google Directions API
- Cancelamento de entregas com restrições de horário
- Autenticação e autorização via JWT
- Tarefa agendada para criação automática das entregas do próximo mês

---

## 📂 Tecnologias Utilizadas

| Categoria           | Tecnologia                                           |
|---------------------|------------------------------------------------------|
| Linguagem           | **Java 21**                                          |
| Framework principal | **Spring Boot 3.5.5**                                |
| Banco de dados      | **MySQL 8**                                          |
| ORM                 | **Spring Data JPA / Hibernate**                      |
| Segurança           | **Spring Security + JWT (io.jsonwebtoken 0.11.5)**   |
| Build               | **Maven**                                            |
| Mapeamento JSON     | **Jackson**                                          |
| Utilitários         | **Lombok**, **RestTemplate**, **Scheduler (Spring)** |
| Arquitetura         | **Em camadas (Controller, Service e Repository)**    |
| APIs externas       | **Google Maps API (Geocoding & Directions)**         |

---
## 🧱 Estrutura do Projeto

```
milkroad/
│
├── src/
│   ├── main/
│   │   ├── java/com/milkroad/
│   │   │   ├── config/                # Configurações globais
│   │   │   ├── controller/            # Controladores REST
│   │   │   ├── dto/                   # DTOs (Data Transfer Objects)
│   │   │   ├── entity/                # Entidades JPA (Cliente, Entrega, etc.)
│   │   │   ├── exception/             # Exceções personalizadas
│   │   │   ├── repository/            # Repositórios (JPA)
│   │   │   ├── security/              # Segurança e autenticação JWT
│   │   │   ├── service/               # Regras de negócio e integrações
│   │   │   └── MilkRoadApplication.java   # Classe principal
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.yml
│   └── test/java/...
├── pom.xml
└── README.md
```
---

## 🔐 Autenticação e Segurança

O sistema utiliza **JWT (JSON Web Token)** para autenticação de usuários.

- O **login** é feito via e-mail e senha criptografada.
- Tokens JWT contêm:
    - `sub`: e-mail do usuário
    - `role`: perfil do usuário
    - `exp`: tempo de expiração (1 hora)
- O filtro `JwtAuthenticationFilter` intercepta e valida os tokens em todas as requisições.
- O `SecurityConfig` define rotas públicas e protegidas.
- `UsuarioDetailsServiceImpl` → Carrega usuários a partir do e-mail fornecido no login.
- `UsuarioDetails` → Adapta entidade Cliente ao modelo do Spring Security.

**Perfis disponíveis:**
- **ADMIN** – acesso total e geração de rotas.
- **CLIENTE** – acesso restrito a dados e entregas pessoais.

---
## 📚 Funcionalidades Principais

| Módulo          | Descrição |
|-----------------|-------------|
| Autenticação    | Login e geração de token JWT |
| Cliente	        | Cadastro, edição, ativação/inativação |
| Entrega         | Geração automática, cancelamento e listagem |
| Rotas	          | Cálculo otimizado de rota diária |
| Geolocalização	 | Conversão de endereço em latitude/longitude |
| Scheduler       | Rotina mensal de criação de entregas |

---
## 👤 Cadastro de Clientes

- **Validações:**
    - Não permite campos vazios
    - E-mail duplicado é bloqueado (banco e back-end)
- **Senha automática:**
    - Criada com os **4 últimos dígitos do celular**
    - Exemplo: `11987654321` → senha `4321`
- **Entregas automáticas:**
    - Após o cadastro, são geradas **entregas de segunda a sexta** para o mês corrente

---
## 📦 Regras de Entregas

- **Geração automática:**  
  Ao cadastrar um cliente, o sistema cria entregas para todos os dias úteis (segunda a sexta).

- **Cancelamento controlado:**  
  Permitido **até as 7h do dia da entrega**. Após isso, é bloqueado com mensagem explicativa.

- **Agendamento mensal:**  
  No dia **28 de cada mês, às 02:00**, o sistema gera automaticamente as entregas do próximo mês, considerando apenas **clientes ativos**.

---
## 📍 Integração com Google Maps

O projeto utiliza duas APIs do Google:

1. **GeoService** → converte endereços em coordenadas (latitude/longitude).
2. **RouteService** → cria rotas diárias otimizadas com base nas entregas confirmadas, iniciando e finalizando no **ADMIN**.

**Configuração:**
```yaml
google:
  api:
    key: SUA_CHAVE_GOOGLE_API
```

Funcionamento:  

O ADMIN é o ponto de partida e chegada (depot), e os clientes ativos são usados como **waypoints**.  
A API retorna a **ordem otimizada** e a **distância total da rota**.

---
## 🕒 Tarefas Automáticas

- Executadas **de segunda a sexta-feira**
- Ignoram **clientes inativos**
- Geração automática de entregas mensais (todo dia 28 às 2h)

---
## ⚙️ Configuração do Ambiente
### Pré-requisitos

- **Java 21+**
- **Maven 3.9+**
- **MySQL 8+**
- **Google Maps API Key válida**

---
### 🔑 Variáveis de ambiente

| Variável | Descrição |
|-----------|------------|
| `ROOT_KEY` | Senha do banco MySQL |
| `JWT_SECRET` | Chave secreta do JWT |
| `GOOGLE_API_KEY` | Chave da API do Google Maps |

---
## 🧩 Endpoints Principais

| Método | Endpoint | Descrição | Permissão |
|--------|-----------|-----------|------------|
| `POST` | `/api/auth/login` | Login e geração do token JWT | Público |
| `POST` | `/api/clientes` | Cadastrar novo cliente (gera senha automática) | ADMIN |
| `GET` | `/api/clientes/ativos` | Listar clientes ativos | ADMIN |
| `PUT` | `/api/clientes/{id}` | Atualizar dados do cliente | ADMIN |
| `DELETE` | `/api/clientes/{id}` | Desativar cliente | ADMIN |
| `GET` | `/api/entregas` | Listar entregas do cliente | CLIENTE |
| `POST` | `/api/entregas/{id}/cancelar` | Cancelar entrega | CLIENTE |
| `GET` | `/api/rotas/{data}` | Gerar rota otimizada | ADMIN |

---
## 🚀 Como Executar o Projeto

### 1️⃣ Clonar o repositório
```bash
git clone https://github.com/Bruna-Leticia12/milkroad-be
cd milkroad-be
```

### 2️⃣ Configurar variáveis de ambiente
```bash
export ROOT_KEY=senha_mysql
export JWT_SECRET=minha_chave_jwt_segura
export GOOGLE_API_KEY=minha_chave_google
```

### 3️⃣ Instalar dependências e executar
```bash
mvn clean install
mvn spring-boot:run
```

### 4️⃣ Acessar o sistema
```
http://localhost:8080
```
---
## 👩‍💻 Autores
- [Bruna Letícia](https://github.com/Bruna-Leticia12)
- [Abdiel Junio](https://github.com/abdieljunio)
- [Lorhayne Lopes](https://github.com/LorhayneLopes)