-module (initAll).
-export ([main/0]).

main() ->
  authenticator:main(),
  exchangeManager:main(),
  replyManager:main(),
  exchange:main("EURONEXT", localhost, 20000)
  exchange:main("NASDAQ", localhost, 20001),
  exchange:main("NYSE", localhost, 20002),
  frontend:main(10000).
