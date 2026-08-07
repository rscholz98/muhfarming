FROM golang:1.25-alpine AS build
WORKDIR /app
RUN apk add --no-cache gcc musl-dev
COPY go.mod ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=1 go build -o /server ./cmd/api

# Litestream binary, copied from the official image (pinned).
FROM litestream/litestream:0.5.16 AS litestream

FROM alpine:3.20
RUN apk add --no-cache libc6-compat
COPY --from=build /server /server
COPY --from=litestream /usr/local/bin/litestream /usr/local/bin/litestream
COPY litestream.yml /etc/litestream.yml
ENV PORT=8080
ENV DB_PATH=/data/muhfarming.db
EXPOSE 8080
# `litestream replicate -exec` restores the DB from S3 on boot if the local
# file is missing, then runs the server and streams the WAL to S3.
CMD ["litestream", "replicate", "-exec", "/server"]
