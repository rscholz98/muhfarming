FROM golang:1.25-alpine AS build
WORKDIR /app
RUN apk add --no-cache gcc musl-dev
COPY go.mod ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=1 go build -o /server ./cmd/api

FROM alpine:3.20
RUN apk add --no-cache libc6-compat
COPY --from=build /server /server
ENV PORT=8080
EXPOSE 8080
CMD ["/server"]
