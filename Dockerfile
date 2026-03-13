FROM --platform=linux/amd64 gradle:7.6.4-jdk11 AS java-build

WORKDIR /workspace

COPY build.gradle /workspace/build.gradle
COPY gradle /workspace/gradle
COPY src /workspace/src
COPY libs /workspace/libs

RUN gradle clean build -x test --no-daemon

FROM --platform=linux/amd64 python:3.12-bookworm AS runtime

ENV PIP_DEFAULT_TIMEOUT=300
ENV PATH="/opt/deepphos-env/bin:${PATH}"

RUN apt-get update \
    && apt-get install -y openjdk-17-jre-headless \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY deepPhosAPI/requirements.txt /tmp/deepphos-requirements.txt
RUN python -m venv /opt/deepphos-env \
    && . /opt/deepphos-env/bin/activate \
    && pip install --upgrade pip setuptools wheel \
    && pip install -r /tmp/deepphos-requirements.txt

COPY deepPhosAPI /app/deepPhosAPI
COPY --from=java-build /workspace/build/libs/pln-0.0.1.jar /app/pln.jar
COPY docker/run-local.sh /usr/local/bin/run-pinet
COPY docker/frontend_proxy.py /usr/local/bin/frontend-proxy.py

RUN chmod +x /usr/local/bin/run-pinet /usr/local/bin/frontend-proxy.py

EXPOSE 5000 8090

CMD ["/usr/local/bin/run-pinet"]
