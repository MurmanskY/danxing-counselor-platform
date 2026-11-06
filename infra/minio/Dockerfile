# Official community MinIO is source-distributed. Build a pinned upstream release.
FROM golang:1.24-bookworm AS build
ENV CGO_ENABLED=0 GOBIN=/out
RUN go install github.com/minio/minio@RELEASE.2025-10-15T17-29-55Z

FROM alpine:3.22
RUN apk add --no-cache ca-certificates && addgroup -g 10001 minio && adduser -D -H -u 10001 -G minio minio && mkdir /data && chown minio:minio /data
COPY --from=build /out/minio /usr/local/bin/minio
USER 10001
EXPOSE 9000 9001
VOLUME /data
ENTRYPOINT ["minio"]
CMD ["server","/data","--console-address",":9001"]
