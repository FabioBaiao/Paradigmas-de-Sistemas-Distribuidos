-module(frontend).
-export([main/1]).

-include("clientSerializer.hrl").

main(Port) ->
  {ok, LSock} = gen_tcp:listen(Port, [binary, {packet, 4}]),
  spawn(fun() -> acceptor(LSock) end).

acceptor(LSock) ->
  {ok, Sock} = gen_tcp:accept(LSock),
  spawn(fun() -> acceptor(LSock) end),
  loggedout(Sock).

% função usada enquanto cliente não autenticado
loggedout(Sock) ->
  receive
    {tcp, Sock, RecvPacket} ->
      case clientSerializer:decode_msg(RecvPacket, 'Request') of
        #'Request'{msg = {auth, #'AuthReq'{username = Username, password = Password}}} ->
          case authenticate(Username, Password) of
            login ->
              SendPacket = #'Reply'{msg = {auth, #'AuthRep'{status = 'LOGIN'}}},
              gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
              loggedin(Sock, Username);
            register ->
              SendPacket = #'Reply'{msg = {auth, #'AuthRep'{status = 'REGISTER'}}},
              gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
              loggedin(Sock, Username);
            wrong_pasword ->
              SendPacket = #'Reply'{msg = {auth, #'AuthRep'{status = 'WRONG_PASSWORD'}}},
              gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
              loggedout(Sock)
          end;
        _ ->
          SendPacket = #'Reply'{msg = {invReq, #'InvalidRequest'{}}},
          gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
          loggedout(Sock)
      end;
    {tcp_closed, Sock} ->
      {tcp_closed};
    {tcp_error, Sock, Reason} ->
      {tcp_error, Reason}
  end.

authenticate(Username, Password) ->
  authenticator ! {authenticate, self(), Username, Password},
  receive
    {authenticator, Res} -> Res
  end.

% função usada enquanto cliente autenticado
loggedin(Sock, User) ->
  receive
    {tcp, Sock, RecvPacket} ->
      case clientSerializer:decode_msg(RecvPacket, 'Request') of
        #'Request'{msg = {order, #'OrderReq'{exchange = Exchange, company = Company, quantity = Quantity, unitPrice = UnitPrice, type = Type}}} ->
          Request = {request, User, Company, Quantity, UnitPrice, Type},
          exchangeManager ! {request, Exchange, Request},
          io:format("New Order~n", []),
          loggedin(Sock, User);
        #'Request'{msg = {logout, #'Logout'{}}} ->
          authenticator ! {logout, self(), User},
          SendPacket = #'Reply'{msg = {logout, #'Logout'{}}},
          gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
          loggedout(Sock);
        _ ->
          SendPacket = #'Reply'{msg = {invReq, #'InvalidRequest'{}}},
          gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
          loggedin(Sock, User)
      end;
    {tcp_closed, Sock} ->
      authenticator ! {logout, self(), User};
    {tcp_error, Sock, _Reason} ->
      authenticator ! {logout, self(), User};

    {sellReply, Company, Quantity, MinPrice, Trades} ->
      TradesList = createTrades(Trades),
      SendPacket = #'Reply'{msg = {order, #'OrderRep'{company = Company, quantity = Quantity, unitPrice = MinPrice, type = 'SELL',
        trades = TradesList}}},
      gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
      loggedin(Sock, User);
    {buyRep, Company, Quantity, MaxPrice, Trades} ->
      TradesList = createTrades(Trades),
      SendPacket = #'Reply'{msg = {order, #'OrderRep'{company = Company, quantity = Quantity, unitPrice = MaxPrice, type = 'BUY',
        trades = TradesList}}},
      gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
      loggedin(Sock, User);
    {tradesRep, Trades} ->
      TradesList = createTrades(Trades),
      SendPacket = #'Reply'{msg = {tradesRep, #'TradesRep'{trades = TradesList}}},
      gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
      io:format("Sent trades to client~n", []),
      loggedin(Sock, User);
    {error, Error} ->
      SendPacket = #'Reply'{msg = {error, Error}},
      gen_tcp:send(Sock, clientSerializer:encode_msg(SendPacket)),
      loggedin(Sock, User)
  end.

% função que cria a estrutura para a serialização das trades
createTrades(Trades) ->
  createTrades(Trades, []).
createTrades([], Trades) ->
  Trades;
createTrades([{Seller, Buyer, Company, Quantity, UnitPrice} | Trades], TradesList) ->
  Trade = #'Trade'{seller = Seller, buyer = Buyer, company = Company, quantity = Quantity, unitPrice = UnitPrice},
  createTrades(Trades, [Trade | TradesList]).
