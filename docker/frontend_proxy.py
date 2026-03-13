#!/usr/bin/env python3
import http.client
import socketserver
from http.server import BaseHTTPRequestHandler


BACKEND_HOST = "127.0.0.1"
BACKEND_PORT = 8091


class ProxyHandler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def do_GET(self):
        self._proxy()

    def do_POST(self):
        self._proxy()

    def do_PUT(self):
        self._proxy()

    def do_DELETE(self):
        self._proxy()

    def do_PATCH(self):
        self._proxy()

    def do_HEAD(self):
        self._proxy()

    def do_OPTIONS(self):
        self._proxy()

    def _proxy(self):
        path = self.path
        if path == "/pinet/api":
            path = "/pln/api"
        elif path.startswith("/pinet/api/"):
            path = "/pln/api/" + path[len("/pinet/api/"):]

        content_length = int(self.headers.get("Content-Length", "0"))
        body = self.rfile.read(content_length) if content_length > 0 else None

        connection = http.client.HTTPConnection(BACKEND_HOST, BACKEND_PORT, timeout=300)
        forward_headers = {}
        for key, value in self.headers.items():
            lower = key.lower()
            if lower in {"host", "connection", "content-length"}:
                continue
            forward_headers[key] = value

        if body is not None:
            forward_headers["Content-Length"] = str(len(body))
        forward_headers["Host"] = self.headers.get("Host", "127.0.0.1:8090")
        forward_headers["X-Forwarded-Proto"] = "http"
        forward_headers["X-Forwarded-Host"] = self.headers.get("Host", "127.0.0.1:8090")

        connection.request(self.command, path, body=body, headers=forward_headers)
        response = connection.getresponse()
        response_body = response.read()

        self.send_response(response.status, response.reason)
        for key, value in response.getheaders():
            lower = key.lower()
            if lower in {"connection", "transfer-encoding", "content-length"}:
                continue
            self.send_header(key, value)
        self.send_header("Content-Length", str(len(response_body)))
        self.end_headers()

        if self.command != "HEAD" and response_body:
            self.wfile.write(response_body)

        connection.close()


class ThreadedTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    allow_reuse_address = True


if __name__ == "__main__":
    with ThreadedTCPServer(("0.0.0.0", 8090), ProxyHandler) as httpd:
        httpd.serve_forever()
