# PiNET MCP Server

This directory contains a minimal stdio MCP server that proxies directly to the PiNET application's native MCP endpoint.

The application also exposes a native HTTP MCP endpoint at:

```text
POST /pln/api/mcp
```

and a small discovery document at:

```text
GET /pln/api/mcp
```

## Run

```bash
PINET_MCP_URL=http://127.0.0.1:8090/pln/api/mcp \
python3 mcp/pinet_mcp_server.py
```

`PINET_MCP_URL` defaults to `http://127.0.0.1:8090/pln/api/mcp`.

## Native application MCP endpoint

Once the PiNET app is running, you can talk to MCP directly through the application API.

Example `initialize` request:

```bash
curl -sS http://127.0.0.1:8090/pln/api/mcp \
  -H 'Content-Type: application/json' \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {"name": "example", "version": "0.0.1"}
    }
  }'
```

## ChatGPT example

Example MCP server entry for ChatGPT:

```json
{
  "mcpServers": {
    "pinet": {
      "command": "python3",
      "args": [
        "/Users/michal/tmp/pinet-server/mcp/pinet_mcp_server.py"
      ],
      "env": {
        "PINET_MCP_URL": "http://127.0.0.1:8090/pln/api/mcp"
      }
    }
  }
}
```

If your app is running elsewhere, change `PINET_MCP_URL` accordingly, for example:

```json
{
  "PINET_MCP_URL": "https://www.pinet-server.org/pinet/api/mcp"
}
```

## Notes

- The server uses stdio with MCP-style `Content-Length` framing.
- It has no third-party dependencies; it relies only on Python's standard library.
- It is a thin stdio-to-HTTP proxy over the application's native MCP endpoint, so the PiNET app must already be running and reachable.
