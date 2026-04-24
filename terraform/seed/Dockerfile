FROM golang:1.22-alpine AS build
WORKDIR /app
COPY go.mod ./
COPY main.go ./
RUN CGO_ENABLED=0 go build -o /server .

FROM public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 AS adapter

FROM alpine:3.20
COPY --from=adapter /lambda-adapter /opt/extensions/lambda-adapter
COPY --from=build /server /server
ENV PORT=8080
ENV AWS_LAMBDA_EXEC_WRAPPER=/opt/extensions/lambda-adapter
EXPOSE 8080
ENTRYPOINT ["/server"]
