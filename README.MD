# Expose REST endpoints through a single SOAP service
## The problem
At the time of writing we were building some REST APIs and then discovered that, due to constraints in our infrastructure, we could only expose SOAP services.

REST support was promised to come Soon™, but until then we had to do something.
And that something was either to build a SOAP implementation for each available REST service or come up with some generic solution.

## The solution

This project solves the problem described previously in a simple, generic and low-effort way.

A Servlet Filter called `RestRoutingFilter` is provided that:

1. Accepts SOAP requests holding HTTP method, endpoint and payload for invoking REST service
1. Invokes REST service as specified by the caller
1. Responds to caller with a SOAP response holding HTTP status code and payload returned by REST endpoint

#### Example
1. Assume we have a REST endpoint that can create Greeting instances. It is available at `/api/greetings` and accepts POST requests with simple JSON payloads.
1. A client then wants to invoke it via SOAP and sends a message to `/soap-api` (where `RestRoutingFilter` is registered):

  ```xml
  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns="http://g.rimsa.lt/rest-over-soap/" >
    <soapenv:Header />
    <soapenv:Body>
      <request method="POST" path="/api/greetings">{ "content" : "Hello!" }</request>
    </soapenv:Body>
  </soapenv:Envelope>
  ```
1. `RestRoutingFilter` then builds a POST request with `{ "content" : "Hello!" }` payload and forwards it to `/api/greetings`
1. REST endpoint creates a Greeting instance and responds with `{ "id" : 123 }`
1. `RestRoutingFilter` wraps it into the following SOAP response and sends it to the caller:

  ```xml
  <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns="http://g.rimsa.lt/rest-over-soap/">
    <SOAP-ENV:Header/>
    <SOAP-ENV:Body>
      <response status="200">{ "id" : 123 }</response>
    </SOAP-ENV:Body>
  </SOAP-ENV:Envelope>
  ```

## Usage
1. Clone this repo and run `gradle publishToMavenLocal` task to install this project into your local Maven repository
1. Add a dependency to your project. For Maven, use the following:

  ```xml
  <dependency>
    <groupId>com.github.grimsa</groupId>
    <artifactId>rest-via-soap</artifactId>
    <version>0.1</version>
  </dependency>
  ```
  For Gradle:

  ```
  dependencies {
     compile 'com.github.grimsa:rest-via-soap:0.1'
  }
  ```

1. Register a filter on some path, e.g. `/soap-api` 

  ```xml
  <filter>
    <filter-name>restRoutingFilter</filter-name>
    <filter-class>com.github.grimsa.restviasoap.RestRoutingFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>restRoutingFilter</filter-name>
    <servlet-name>/soap-api</servlet-name>
  </filter-mapping>
  ```
1. Smile and send some SOAP requests to `/soap-api`

## Limitations
* HTTP Redirects are not supported