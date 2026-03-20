IMAGE ?= pinet-local-prebuilt
PLATFORM ?= linux/amd64
ODC_IMAGE ?= owasp/dependency-check:latest
ODC_DATA_DIR ?= /tmp/odc-data
ODC_REPORT_DIR ?= dependency-check-report

.PHONY: build-image security-scan security-scan-deepphos

build-image:
	docker build --platform $(PLATFORM) -t $(IMAGE) .

security-scan:
	rm -rf build $(ODC_REPORT_DIR)
	mkdir -p $(ODC_DATA_DIR) $(ODC_REPORT_DIR)
	docker run --rm \
		-v $(CURDIR):/src \
		-v $(ODC_DATA_DIR):/usr/share/dependency-check/data \
		$(ODC_IMAGE) \
		--noupdate \
		--scan /src/build.gradle \
		--scan /src/libs \
		--scan /src/src \
		--exclude /src/src/main/resources/static/js/node_modules/jquery-ui/external/** \
		--exclude /src/src/main/resources/static/js/node_modules/jquery-ui/ui/jquery-1-7.js \
		--exclude /src/dependency-check-report/** \
		--format JSON \
		--out /src/$(ODC_REPORT_DIR) \
		--project pinet-server

security-scan-deepphos:
	rm -rf $(ODC_REPORT_DIR)
	mkdir -p $(ODC_DATA_DIR) $(ODC_REPORT_DIR)
	docker run --rm \
		-v $(CURDIR):/src \
		-v $(ODC_DATA_DIR):/usr/share/dependency-check/data \
		$(ODC_IMAGE) \
		--noupdate \
		--scan /src/deepPhosAPI \
		--exclude /src/deepPhosAPI/env/** \
		--exclude /src/deepPhosAPI/.git/** \
		--exclude /src/dependency-check-report/** \
		--format JSON \
		--out /src/$(ODC_REPORT_DIR) \
		--project deepPhosAPI
