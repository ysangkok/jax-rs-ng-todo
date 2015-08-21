JAX-RS/SQLite backend for ng-todo
---

1. clone this repo and enter the cloned directory
1. `cd jax-rs-ng-todo/topack`
1. `git clone git://github.com/vojtajina/ng-todo.git`
1. `cd ng-todo`
1. `git checkout d65cf92d`
1. `bower install`
1. edit `todo.js` so that the `api.mongolab.com` URL becomes `/rest/items/:id`
1. `cd ../..`
1. `make`
1. when Jetty is launched (by Make), open a browser at http://localhost:8082/ng-todo/
