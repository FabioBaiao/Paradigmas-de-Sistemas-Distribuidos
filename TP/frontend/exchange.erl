-module (exchange).
-export ([main/3]).

-include ("exchangeSerializer.hrl").

main(Exchange, Address, Port) ->
  spawn(fun() -> init_exchange(Exchange, Address, Port) end).

init_exchange(Exchange, Address, Port) ->
  {ok, Sock} = gen_tcp:connect(Address, Port, [binary, {packet, 1}]),
  exchangeManager ! {new_exchange, self(), Exchange},
  exchange(Sock).

exchange(Sock) ->
  receive
    {request, User, Company, Quantity, UnitPrice, Type} ->
      SendPacket = #'Request'{order = #'Order'{user = User, company = Company, quantity = Quantity, unitPrice = UnitPrice, type = Type}},
      gen_tcp:send(Sock, exchangeSerializer:encode_msg(SendPacket)),
      exchange(Sock);

    {tcp, Sock, RecvPacket} ->
      case exchangeSerializer:decode_msg(RecvPacket, 'Reply') of
        #'Reply'{order = #'Order'{user = User, company = Company, quantity = Quantity, unitPrice = UnitPrice, type = Type},
            trades = RecvTrades} ->
          Trades = extractTrades(RecvTrades),
          replyManager ! {reply, User, Company, Quantity, UnitPrice, Type, Trades},
          exchange(Sock);
        #'Reply'{error = #'ErrorMsg'{user = User, error = Error}} ->
          replyManager ! {error, User, Error},
          exchange(Sock);
        #'Reply'{trades = RecvTrades} ->
          Trades = extractTrades(RecvTrades),
          replyManager ! {trades, Trades},
          exchange(Sock)
      end
  end.

% extrai as trades realizadas, em tuplos simples
extractTrades(RecvTrades) ->
  extractTrades(RecvTrades, []).
extractTrades([], L) ->
  L;
extractTrades([#'Trade'{seller = Seller, buyer = Buyer, company = Company, quantity = Quantity, unitPrice = UnitPrice} | RecvTrades], L) ->
  extractTrades(RecvTrades, [{Seller, Buyer, Company, Quantity, UnitPrice} | L]).
