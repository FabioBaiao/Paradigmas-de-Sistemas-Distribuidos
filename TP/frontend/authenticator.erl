-module (authenticator).
-export ([main/0]).


main() ->
  register(?MODULE, spawn(fun() -> authenticator(#{}) end)).

authenticator(Map) ->
  receive
    {authenticate, From, Username, Password} ->
      case maps:find(Username, Map) of
        {ok, {Password, Pids}} ->
          case sets:size(Pids) of
             0 -> replyManager ! {userLogin, Username, From}
          end,
          From ! {?MODULE, login},
          authenticator(maps:put(Username, {Password, sets:add_element(From, Pids)}, Map));
        {ok, _} ->
          From ! {?MODULE, wrong_pasword},
          authenticator(Map);
        error ->
          From ! {?MODULE, register},
          Pids = sets:new(),
          authenticator(maps:put(Username, {Password, sets:add_element(From, Pids)}, Map))
      end;
    {pids, From, User} ->
      case maps:find(User, Map) of
        {ok, {_, Pids}} ->
          From ! {?MODULE, sets:to_list(Pids)},
          authenticator(Map)
      end;
    {logout, From, User} ->
      case maps:find(User, Map) of
        {ok, {Password, Pids}} ->
          authenticator(maps:put(User, {Password, sets:del_element(From, Pids)}, Map))
      end
  end.
