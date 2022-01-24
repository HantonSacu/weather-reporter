.PHONY : lint codeformat-check codeformat-fix uberjar test develop

lint :
	clojure -M:clj-kondo --lint src test

uberjar :
	lein uberjar

test :
	bin/kaocha

develop :
	clj -M:frontend:dev

codeformat-check :
	lein cljfmt check

codeformat-fix :
	lein cljfmt fix
