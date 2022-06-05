.PHONY: check run
check:
		./gradlew lint spotlessApply spotlessCheck spotlessKotlin detekt ktlintCheck --profile --daemon

run:
		./gradlew build

.DEFAULT_GOAL := check
