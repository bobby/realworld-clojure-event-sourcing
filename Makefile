SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

# Dev Util

.PHONY: bootstrap
bootstrap:
	brew install clojure tokei

.PHONY: outdated
outdated:
	clojure -R:dev:test:build -M:outdated

.PHONY: deps
deps: deps.edn
	clojure -R:dev:test:build -Stree

.PHONY: clean
clean:
	rm -rf target/

# Dev Workflow

DOCKER_COMPOSE_YML:=docker-compose.yml
STACKNAME:=conduit-services

.PHONY: start-services
start-services:
	docker stack deploy -c $(DOCKER_COMPOSE_YML) $(STACKNAME)

.PHONY: stop-services
stop-services:
	docker stack rm $(STACKNAME)

CLJ_REPL_ALIAS:=

.PHONY: repl
repl:
	clojure -M:dev${CLJ_REPL_ALIAS}

.PHONY: dev
dev: start-services repl

.PHONY: clj-test
clj-test:
	$(MAKE) -C ../common test
	clojure -M:test:runner

.PHONY: api-test
api-test:
	APIURL=http://localhost:8080/api ./test/run-api-tests.sh

.PHONY: test
test: api-test clj-test

.PHONY: run
run: start-services
	clojure -M:run

# Release

VERSION:=$(shell git rev-parse --short=10 HEAD)

target/classes/redis_cafe/storefront.class: deps.edn src/
	clojure -M:build -m package

.PHONY: build
build: target/classes/redis_cafe/storefront.class

TAG:=customers
DOCKER_OPTS:=

.PHONY: docker
docker:
	cd .. && docker build . -t $(TAG) -f Dockerfile.storefront $(DOCKER_OPTS)

# Project info

.PHONY: loc
loc:
	tokei build/ config/ dev/ resources/ src/ test/ Makefile *.edn
