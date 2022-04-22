# Vapor

A video game rental web application.

## Design

Built using a [rules engine](https://github.com/oakes/odoyle-rules), this puts state changes in the drivers seat in a
re-framisk way that's already coupled with a fact database like datascript. These rules can be used on the client and the server,
thus unifying the data-pipline.

As a brief intro to the rules engline, consider this rule from the vapor/core ns. Every rule has a set of facts that
determine if the rule runs or re-runs. Here is the facts for the `app-root`

```clojure
     [::global ::selected-game selected-game?]
     [::global ::view-cart view-cart?]
     [::global ::search-for-game search-for-game?]
```

These facts now serve is a way to determine which part of the application to
load. In this application, when ever the user searches for a game, we send that
fact back to the server, where it makes a request to the [Giant Bomb Api](https://www.giantbomb.com/api/).
The response (games) are then added to the facts.

This project is a heavily modified version of [odoyle-rum-todo](https://github.com/oakes/odoyle-rum-todo).
It includes both a server and a client, in start.clj and start.cljs.

## Development

add a file called "secrets.edn" to resources with the following contents:

```
{:giant-bomb {:api_key <YOUR GIANT BOMB API KEY>}}

```

Start a repl in your editor, choose the clojure-cli REPL. Then eval the dev.clj
namespace forms to create the client and start the webserver. Note, your repl
won't be tracking browser (cljs) state.
