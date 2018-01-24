-module (replyManager).
-export ([main/0]).


main() ->
  register(?MODULE, spawn(fun() -> replyManager(#{}, #{}) end)).

% UserOrders é um map<username, [Order]>
% UserTrades é um map<username, [Trade]>
replyManager(UserOrders, UserTrades) ->
  receive
    {reply, User, Company, Quantity, MinPrice, 'SELL', Trades} ->
      SellReply = {sellReply, Company, Quantity, MinPrice, Trades},
      NewUserOrders = reply(User, SellReply, UserOrders),
      BuyerTrades = groupBuyers(Trades),
      NewUserTrades = sendTrades(maps:keys(BuyerTrades), BuyerTrades, UserTrades),
      replyManager(NewUserOrders, NewUserTrades);
    {reply, User, Company, Quantity, MaxPrice, 'BUY', Trades} ->
      BuyReply = {buyReply, Company, Quantity, MaxPrice, Trades},
      NewUserOrders = reply(User, BuyReply, UserOrders),
      SellerTrades = groupSellers(Trades),
      NewUserTrades = sendTrades(maps:keys(SellerTrades), SellerTrades, UserTrades),
      replyManager(NewUserOrders, NewUserTrades);
    {trades, Trades} ->
      BuyerTrades = groupBuyers(Trades),
      SellerTrades = groupSellers(Trades),
      NewUserTrades1 = sendTrades(maps:keys(BuyerTrades), BuyerTrades, UserTrades),
      NewUserTrades2 = sendTrades(maps:keys(SellerTrades), SellerTrades, NewUserTrades1),
      io:format("Sent Trades to user~n", []),
      replyManager(UserOrders, NewUserTrades2);
    {error, User, Error} ->
      Pids = getPids(User),
      sendReply({error, Error}, Pids),
      replyManager(UserOrders, UserTrades);
    {userLogin, User, Pid} ->
      NewUserOrders = sendUserOrders(User, Pid, UserOrders),
      NewUserTrades = sendUserTrades(User, Pid, UserTrades),
      replyManager(NewUserOrders, NewUserTrades)
  end.

% envia ordens guardadas
% retorna novo UserOrders
sendUserOrders(User, Pid, UserOrders) ->
  case maps:find(User, UserOrders) of
    {ok, Orders} ->
      sendOrders(Pid, Orders),
      maps:remove(User, UserOrders);
    error ->
      UserOrders
  end.

%envia trades guardadas
% retorna novo UserTrades
sendUserTrades(User, Pid, UserTrades) ->
  case maps:find(User, UserTrades) of
    {ok, Trades} ->
      Pid ! {tradesRep, Trades},
      io:format("Offline Trades Sent~n", []),
      maps:remove(User, UserTrades);
    error ->
      io:format("No Offline Trades~n", []),
      UserTrades
  end.

% envia orders separadamente
sendOrders(_, []) ->
  ok;
sendOrders(Pid, [Order | Orders]) ->
  Pid ! Order,
  sendOrders(Pid, Orders).

% recebe pids de atores ativos referentes ao User
getPids(User) ->
  authenticator ! {pids, self(), User},
  receive
    {authenticator, Res} -> Res
  end.

% responde ao cliente que adicionou a order
% retorna novo UserOrders
reply(User, Reply, UserOrders) ->
  case getPids(User) of
    [] ->
      addMapList(User, Reply, UserOrders);
    Pids ->
      sendReply(Reply, Pids),
      UserOrders
  end.

% adiciona elemento à lista de uma key
% retorna novo Map
addMapList(Key, Element, Map) ->
  case maps:find(Key, Map) of
    {ok, Elements} ->
      maps:put(Key, [Element | Elements], Map);
    error ->
      maps:put(Key, [Element], Map)
  end.

% envia o resultado da order a todas as sessões
sendReply(_, []) ->
  ok;
sendReply(Reply, [Pid | Pids]) ->
  Pid ! Reply,
  sendReply(Reply, Pids).

% agrupa trades por buyers
groupBuyers(Trades) ->
  groupBuyers(Trades, #{}).
groupBuyers([], BuyerTrades) ->
  BuyerTrades;
groupBuyers([{_, Buyer, _, _, _} = Trade | Trades], BuyerTrades) ->
  io:format("grouping buyers: ~p~n", [Buyer]),
  NewBuyerTrades = addMapList(Buyer, Trade, BuyerTrades),
  groupBuyers(Trades, NewBuyerTrades).

% agrupa trades por sellers
groupSellers(Trades) ->
  groupSellers(Trades, #{}).
groupSellers([], SellerTrades) ->
  SellerTrades;
groupSellers([{Seller, _, _, _, _} = Trade | Trades], SellerTrades) ->
  NewSellerTrades = addMapList(Seller, Trade, SellerTrades),
  groupSellers(Trades, NewSellerTrades).

% envia trades aos users
% retorna novo UserTrades
sendTrades([], _, UserTrades) ->
  io:format("No more users~n"),
  UserTrades;
sendTrades([User | Users], MapTrades, UserTrades) ->
  io:format("Sending trades to ~p~n", [User]),
  case getPids(User) of
    [] ->
      io:format("No pids~n", []),
      NewUserTrades = addTrades(User, MapTrades, UserTrades),
      sendTrades(Users, MapTrades, NewUserTrades);
    Pids ->
      Trades = getTrades(User, MapTrades),
      sendReply({tradesRep, Trades}, Pids),
      sendTrades(Users, MapTrades, UserTrades)
  end.

getTrades(User, MapTrades) ->
  maps:get(User, MapTrades).

% adiciona trades de users offline
% retorna novo UserTrades
addTrades(User, MapTrades, UserTrades) ->
  case maps:find(User, UserTrades) of
    {ok, SavedTrades} ->
      maps:put(User, lists:append(SavedTrades, maps:get(User, MapTrades)), UserTrades);
    error ->
      maps:put(User, maps:get(User, MapTrades), UserTrades)
  end.
