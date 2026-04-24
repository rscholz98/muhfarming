FROM golang:1.25-alpine AS build
WORKDIR /app
COPY go.mod ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o /server ./cmd/api

# Lambda-compatible runtime image. The AWS Lambda Web Adapter forwards
# Lambda invocations to a regular HTTP server listening on $PORT, so the
# Go application code does not need any Lambda-specific changes.
FROM public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 AS adapter

FROM alpine:3.20
COPY --from=adapter /lambda-adapter /opt/extensions/lambda-adapter
COPY --from=build /server /server

ENV PORT=8080
ENV AWS_LAMBDA_EXEC_WRAPPER=/opt/extensions/lambda-adapter

EXPOSE 8080
ENTRYPOINT ["/server"]
FROM golang:1.25-alpine AS build
WORKDIR /app
COPY go.mod ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o /server ./cmd/api

FROM alpine:3.20
COPY --from=build /server /server
EXPOSE 8080
ENTRYPOINT ["/server"]
