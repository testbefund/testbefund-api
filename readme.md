## Build
To build the application, follow these steps:

1. Execute the gradle task ``bootJar``
2. Run ``docker-compose up``

This will build the jar, package it into a docker container and let's compose run it.

### Docker Envs
The following environment variables are available and have to be set.
* ``DB_URL`` is the JDBC connection string
* ``DB_USER`` is the database user
* ``DB_PASS`` is the database password
* ``APP_USER`` is the application root user
* ``APP_PASS`` is the application root password

THe following environment variables are available and not mandatory.
* ``DB_DRIVER_CLASS_NAME`` is the java class name for the JDBC driver. Defaults to ``org.postgresql.Driver``
* ``HIBERNATE_DDL_AUTO`` Defines the schema update policy. Defaults to ``update``

Additionally, you may overwrite any property defined by spring or defined in the application.yml via environment variables
by substituting dots ('.') with underscores ('_'), e.g ``TESTBEFUND_USER`` for ``testbefund.user``

## API
The API can be explored with swagger-ui. 
1. Start the service, either with docker-compose or as Spring Boot run configuration
2. Go to ``http://localhost:8080/swagger-ui.html`` in your browser

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
