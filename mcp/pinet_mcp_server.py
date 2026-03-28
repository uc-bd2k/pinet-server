#!/usr/bin/env python3
import json
import os
import sys
import urllib.error
import urllib.request
from typing import Any, Dict, Optional


MCP_URL = os.environ.get("PINET_MCP_URL", "http://127.0.0.1:8090/pln/api/mcp")


def _read_message() -> Optional[Dict[str, Any]]:
    headers: Dict[str, str] = {}
    while True:
        line = sys.stdin.buffer.readline()
        if not line:
            return None
        if line in (b"\r\n", b"\n"):
            break
        key, _, value = line.decode("utf-8").partition(":")
        headers[key.strip().lower()] = value.strip()

    length = int(headers.get("content-length", "0"))
    if length <= 0:
        return None
    payload = sys.stdin.buffer.read(length)
    return json.loads(payload.decode("utf-8"))


def _write_message(message: Dict[str, Any]) -> None:
    payload = json.dumps(message).encode("utf-8")
    sys.stdout.buffer.write(f"Content-Length: {len(payload)}\r\n\r\n".encode("ascii"))
    sys.stdout.buffer.write(payload)
    sys.stdout.buffer.flush()


def _post_to_mcp(message: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    body = json.dumps(message).encode("utf-8")
    request = urllib.request.Request(
        MCP_URL,
        data=body,
        headers={"Content-Type": "application/json", "Accept": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request) as response:
            raw = response.read().decode("utf-8")
            if not raw.strip():
                return None
            try:
                return json.loads(raw)
            except json.JSONDecodeError:
                return {
                    "jsonrpc": "2.0",
                    "id": message.get("id"),
                    "error": {
                        "code": -32000,
                        "message": f"Non-JSON response from {MCP_URL}: {raw[:500]}",
                    },
                }
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        return {
            "jsonrpc": "2.0",
            "id": message.get("id"),
            "error": {"code": -32000, "message": f"HTTP {exc.code} from {MCP_URL}: {detail}"},
        }
    except urllib.error.URLError as exc:
        return {
            "jsonrpc": "2.0",
            "id": message.get("id"),
            "error": {"code": -32000, "message": f"Request failed for {MCP_URL}: {exc.reason}"},
        }
    except Exception as exc:
        return {
            "jsonrpc": "2.0",
            "id": message.get("id"),
            "error": {"code": -32000, "message": f"Unexpected proxy error for {MCP_URL}: {exc}"},
        }


def main() -> int:
    while True:
        message = _read_message()
        if message is None:
            return 0
        response = _post_to_mcp(message)
        if response is not None:
            _write_message(response)


if __name__ == "__main__":
    raise SystemExit(main())
