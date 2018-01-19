-module (exchangeManager).
-export ([main/0]).

main() ->
  register(?MODULE, spawn(fun() -> exchangeManager(#{}) end)).

exchangeManager(Map) ->
  receive
    {request, Exchange, Request} ->
      case maps:find(Exchange, Map) of
        {ok, Pid} ->
          Pid ! Request,
          exchangeManager(Map)
      end;
    {new_exchange, From, Exchange} ->
      exchangeManager(maps:put(Exchange, From, Map))
  end.
