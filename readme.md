## Build
To build the application, follow these steps:

1. Execute the gradle task ``bootJar``
2. Run ``docker-compose up``

This will build the jar, package it into a docker container and let's compose run it.

## Mapstruct
We are using mapstruct to allow us to create multiple different views of objects, like the test-container read 
view, which excludes sensitive (write-enabling) data. 

Mapstruct is a compile-time annoation processor and DTO mapping framework and allows us to present
data in multiple different views using the same Java class model. 

### Deployment

## Minikube

```sh
$ minikube start
```

```sh
$ kubectl apply -k kustomize
```
