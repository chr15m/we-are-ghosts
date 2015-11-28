CSS=build/css/style.css
IDX=build/index.html
APP=build/js/compiled/we_are_ghosts.js
ME=$(shell basename $(shell pwd))

all: $(APP) $(CSS) $(IDX)

build:
	mkdir -p build

$(CSS): resources/public/css/style.css
	mkdir -p build/css
	cp $< $@

$(APP): src/**/** project.clj
	rm -f $(APP)
	lein cljsbuild once min

$(IDX): resources/public/index.html
	cp $< $@

clean:
	lein clean
	rm -rf build
