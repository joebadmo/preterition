# preterition

Configured with a github repository and post-commit hook, it:

1. parses yaml front-matter markdown pages
2. updates documents in postgres database
3. provides http api to documents

## Build

  lein do clean, cljsbuild once static, cljsbuild once prod, export-assets, uberjar
  vagrant up
  vagrant ssh
  cp /vagrant/Dockerfile .
  cp /vagrant/target/uberjar/preterition-0.1.0-SNAPSHOT-standalone.jar ./preterition.jar
  docker build -t joebadmo/preterition .
  docker push

## License

MIT
