OnTrackRetail Exercise
====================


### API versioning

In here we are going to address a few possible solutions to versioning REST resources using several widespread patterns.


#### URL based versioning

One of the common approaches used in modern REST programming is to fully describe a resource available entirely
via an URL. Parts of the community argues this is the way forward as only one descriptor is required to fully qualify
a resource.

Example:

```
GET /api/profile/$version
```

This is usually accompanied by something that accepts `latest` as a valid version and relies on the backend to guarantee
that the handling of `latest` is transparent to any API consumer. So the following call would return the latest chronological
version of a resource.

```
GET /api/profile/_latest
```


#### Header based versioning

Another common practice is to specify the version of the resource using a known header, that is known to be the same
by both the client and the server. This approach does have a drawback, as it forces all clients to be implemented
in a certain way such that 

While HTTP request headers are part of the standard HTTP protocol, a custom header name is not, and as a result we now
have to manage this from both the server and client side, while still having to maintain a "latest" version guarantee
for any consumer, just for convenience.