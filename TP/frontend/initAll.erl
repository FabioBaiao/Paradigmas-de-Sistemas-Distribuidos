-module (initAll).
-export ([main/0]).

main() ->
  newauthenticator:main(),
  %exchangeManager:main(),
  %replyManager:main(),
  %exchange:main("NASDAQ", localhost, 20000),
  newfrontend:main(10000).
