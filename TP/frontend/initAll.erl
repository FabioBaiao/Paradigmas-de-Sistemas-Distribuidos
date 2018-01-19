-module (initAll).
-export ([main/0]).

main() ->
  authenticator:main(),
  exchangeManager:main(),
  replyManager:main(),
  %exchange:main("NASDAQ", localhost, 20000),
  frontend:main(10000).
