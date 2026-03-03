
# MindAgent

An AI agent platform built with **Spring Boot + Spring AI + PostgreSQL/pgvector + React (TypeScript)**.  
MindAgent focuses on **Agent Loop, tool calling, RAG knowledge retrieval, multi‑model orchestration and real‑time execution visualization** – not just “call an LLM API”.

---

### Features

- **Agent Loop (Think‑Execute + state machine)**  
  Multi‑step reasoning with explicit states (`IDLE / THINKING / EXECUTING / DONE / ERROR`), max‑step guard and error handling to avoid infinite tool loops.
- **Tool calling framework**  
  Pluggable tool system (`Tool` interface + fixed/optional tool sets) on top of Spring AI, with **manual control over tool execution** via `ToolCallingManager`.
- **RAG knowledge base (PostgreSQL + pgvector)**  
  Markdown parsing, chunking, embedding persistence, pgvector similarity search and ivfflat index for \(10^5+\) vectors.
- **Multi‑model routing (Spring AI)**  
  `MultiChatClientConfig` + `ChatClientRegistry` to switch between DeepSeek / ZhiPu GLM and support future models without invasive changes.
- **Real‑time UI (SSE)**  
  Server‑Sent Events stream the agent’s planning/thinking/executing status and generated content to the React UI.
- **Modern web UI**  
  Minimal black‑&‑white React interface, agent management, chat sessions, knowledge base and document management.

---

### Architecture

Backend (`jchatmind/` → MindAgent service):

- **Spring Boot 3 + Spring AI**
- Controllers (`/api/*`): agents, chat sessions, chat messages, knowledge bases, documents, tools, SSE.
- Facade services: orchestration layer for persistence + agent runtime.
- `JChatMindFactory` (agent factory) + `JChatMind` (Agent Loop implementation).
- PostgreSQL + pgvector for both structured data and vector search.

Frontend (`ui/` → React client):

- React + TypeScript + Vite
- Ant Design + Ant Design X chat components
- Pages: Agent chat, chat history, knowledge base, documents.
- SSE client for real‑time agent status & messages.

---

### Project Structure

```text
MindAgent
├─ mindagent-service/  # Spring Boot backend (Maven module 'mindagent')
│  ├─ src/main/java/com/kama/jchatmind/...
│  └─ src/main/resources/...
├─ mindagent-ui/       # React + Vite frontend
│  └─ src/...
└─ README.md
```

> For GitHub you can name the repository `mindagent` – the folder name on your machine does not affect the code.

---

### Getting Started

#### 1. Prerequisites

- JDK **17**
- Node.js **18+** and npm
- PostgreSQL **15+** with **pgvector** extension installed
- DeepSeek / ZhiPu (GLM‑4.6) API keys (optional but recommended)

---

#### 2. Backend – MindAgent service

```bash
cd mindagent-service

# On Windows (using Maven Wrapper)
mvnw.cmd clean spring-boot:run

# On macOS / Linux
./mvnw clean spring-boot:run
```

By default the backend runs at `http://localhost:8080`.

Key configuration file:

- `jchatmind/src/main/resources/application.yaml`  
  - PostgreSQL connection (host / port / database / username / password)  
  - pgvector config  
  - Spring AI model keys (DeepSeek, ZhiPu, etc.)

---

#### 3. Frontend – React client

```bash
cd mindagent-ui
npm install
npm run dev
```

The dev server typically runs at `http://localhost:5173` (check the terminal output).  
The frontend expects the backend at `http://localhost:8080`.

---

### How to Use

1. **Create an Agent**
   - Open the app → `Agents` tab → click **New agent**.
   - Configure model (DeepSeek / GLM‑4.6), system prompt, allowed tools and knowledge bases.

2. **Add Knowledge Base**
   - Go to `Knowledge Base` tab → **New knowledge base**.
   - Open a knowledge base and upload Markdown documents (RAG pipeline will ingest and index them).

3. **Start a Conversation**
   - In the main chat view, select an agent and type a message.
   - Watch the agent status chips (`Planning / Thinking / Executing`) and the streamed responses.