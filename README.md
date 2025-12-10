# MindAgent

MindAgent is a Java agent application for configuring models and tools, uploading Markdown knowledge bases, and viewing conversations and tool results in a web interface.

The backend uses Java 17, Spring Boot 3.5, Spring AI 1.1, MyBatis, and PostgreSQL with pgvector. The frontend uses React 19 and TypeScript.

## Implementation

A chat request saves the user message and submits a background task. `MindAgentFactory` assembles the model, conversation history, tools, and knowledge bases. `MindAgent` then runs a reasoning and execution loop for up to 20 steps. `ToolCallingManager` executes requested tools; their results are persisted and included in subsequent model context.

- **Task scheduling:** The executor has 4 core threads, up to 10 threads, and a queue of 100 tasks. Each session accepts one active task. Duplicate requests and full queues return a business error and roll back the message write. Tasks read history after the message transaction commits. These limits apply to one service instance.
- **Live updates:** SSE publishes complete messages and execution status. Token streaming and event replay are not implemented. Background tasks continue after a browser disconnects; reopening a session loads persisted messages.
- **Knowledge retrieval:** Markdown is split by heading. Ollama's `bge-m3` generates 1024-dimensional embeddings. Retrieval filters by knowledge base and uses L2 distance. The database script includes an IVFFlat index; rebuild and tune it after importing data according to dataset size.
- **Tools:** Tools implement a common interface and can be enabled per agent. Database queries use read-only transactions, a 5-second timeout, and a 100-row limit. Use a dedicated read-only database account with appropriately restricted access in production.
- **Model routing:** `ChatClientRegistry` selects the configured model. DeepSeek and Zhipu are supported.

## Development stages

| Stage | Scope |
| --- | --- |
| 1 | Backend services, model configuration, agent loop, tools, sessions, and persistence |
| 2 | Markdown parser refactoring and regression tests for formatting and concurrent requests |
| 3 | React workspace for agent configuration, knowledge management, conversations, and SSE updates |
| 4 | Docker services, configuration examples, and setup documentation |

Text before the first Markdown heading is retained as a separate section. Tables and code blocks keep their source formatting. Document parsing uses no shared request state.

## Layout

```text
mindagent-service/
  src/main/java/com/mind/
    agent/          Agent loop, factory, and tools
    controller/     REST and SSE endpoints
    service/        Conversations, documents, retrieval, and task scheduling
    mapper/         MyBatis data access
    model/          Entities, DTOs, views, requests, and responses
    event/          Chat events and listeners
  src/main/resources/
    mapper/         SQL mappings
    schema.sql      Tables and indexes
mindagent-ui/       React interface
docker-compose.yml
```

## Local setup

Install JDK 17, Node.js 20.19+ (or 22.12+), and Docker. Model API keys are also required.

```bash
docker compose up -d
docker compose exec ollama ollama pull bge-m3

cd mindagent-service
export DEEPSEEK_API_KEY=your_api_key
export ZHIPUAI_API_KEY=your_api_key
./mvnw spring-boot:run
```

The backend listens on port `8080`. `GET /health` checks service availability. Startup runs `schema.sql`; the database account needs permission to create extensions and tables. Check existing database schemas before starting: `IF NOT EXISTS` does not migrate existing columns.

Start the frontend in another terminal:

```bash
cd mindagent-ui
cp .env.example .env
npm install
npm run dev
```

Open the URL printed in the terminal and create an agent to start chatting. To use retrieval, create a knowledge base, upload Markdown documents, and associate it with the agent.

### Environment variables

| Variable | Default or purpose |
| --- | --- |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `mindagent` |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres`; local development only |
| `DB_INIT_MODE` | `always`; use `never` for a prepared database |
| `DEEPSEEK_API_KEY` / `ZHIPUAI_API_KEY` | Model API keys |
| `OLLAMA_BASE_URL` | `http://localhost:11434` |
| `DOCUMENT_STORAGE_PATH` | `./data/documents` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP account and application password |
| `VITE_API_BASE_URL` | Backend URL, configured in the frontend `.env` file |

## Validation

```bash
cd mindagent-service
./mvnw -Dtest=MarkdownParserServiceTest,ChatTaskDispatcherTest,SseServiceImplTest,DataBaseToolsTest test

cd ../mindagent-ui
npm run build
```

Some demonstrations in `src/test` require model endpoints and a database. Configure those services before running the demonstrations.

## Current scope

The application supports local demonstrations and single-instance deployments. Authentication, user data isolation, coordination across instances, and task recovery are not implemented. Email and file-writing tools perform real operations; review their configuration and scope before enabling them.
