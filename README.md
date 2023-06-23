# What is this project?

This is a ten-percent project to explore the possibilities of GraphQL in the Content API.

# How do I run it?

1. Get hold of the `content-api` repository and follow the instructions there to set up a local docker-based Elasticsearch
instance and restore a CAPI snapshot into it.
2. 
```
sbt run
```

(or do the same in your favourite debugger)

3. To view the schema, go to http://localhost:9000/schema/content
4. To make a GraphQL request, go to http://localhost:9000/query.  You will need to "fake" the Kong authentication
header, set a header called `X-Consumer-Username` to the value `:internal` (note the `:`) in order for the API to work.