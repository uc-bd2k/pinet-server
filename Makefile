IMAGE ?= pinet-local-prebuilt
PLATFORM ?= linux/amd64

.PHONY: build-image

build-image:
	docker build --platform $(PLATFORM) -t $(IMAGE) .
