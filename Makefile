IMAGE ?= pinet-local-prebuilt
PLATFORM ?= linux/amd64
ODC_IMAGE ?= owasp/dependency-check:latest
ODC_DATA_DIR ?= /tmp/odc-data
ODC_REPORT_DIR ?= dependency-check-report

.PHONY: build-image security-scan

build-image:
	docker build --platform $(PLATFORM) -t $(IMAGE) .

security-scan:
	mkdir -p $(ODC_DATA_DIR) $(ODC_REPORT_DIR)
	docker run --rm \
		-v $(CURDIR):/src \
		-v $(ODC_DATA_DIR):/usr/share/dependency-check/data \
		$(ODC_IMAGE) \
		--noupdate \
		--scan /src \
		--exclude /src/build/** \
		--exclude /src/build/libs/** \
		--exclude /src/dependency-check-report/** \
		--format JSON \
		--out /src/$(ODC_REPORT_DIR) \
		--project pinet-server
