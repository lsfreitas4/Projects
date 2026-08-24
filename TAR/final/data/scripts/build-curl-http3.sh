#!/bin/bash
set -e

H3="$HOME/h3"
SRC="$HOME/h3-src"

OPENSSL_VERSION="3.5.0"
NGHTTP3_VERSION="v1.15.0"
NGTCP2_VERSION="v1.22.1"
CURL_VERSION="8.19.0"

mkdir -p "$H3" "$SRC"
cd "$SRC"

echo "[1/4] Building OpenSSL $OPENSSL_VERSION..."
if [ ! -d openssl ]; then
  git clone --depth 1 --branch "openssl-$OPENSSL_VERSION" https://github.com/openssl/openssl
fi

cd "$SRC/openssl"
./config --prefix="$H3/openssl" --libdir=lib
make -j"$(nproc)"
make install_sw

echo "[2/4] Building nghttp3 $NGHTTP3_VERSION..."
cd "$SRC"
if [ ! -d nghttp3 ]; then
  git clone --depth 1 --branch "$NGHTTP3_VERSION" https://github.com/ngtcp2/nghttp3
fi

cd "$SRC/nghttp3"
git submodule update --init
autoreconf -fi
./configure --prefix="$H3/nghttp3" --enable-lib-only
make -j"$(nproc)"
make install

echo "[3/4] Building ngtcp2 $NGTCP2_VERSION..."
cd "$SRC"
if [ ! -d ngtcp2 ]; then
  git clone --depth 1 --branch "$NGTCP2_VERSION" https://github.com/ngtcp2/ngtcp2
fi

cd "$SRC/ngtcp2"
autoreconf -fi

PKG_CONFIG_PATH="$H3/openssl/lib/pkgconfig:$H3/nghttp3/lib/pkgconfig" \
LDFLAGS="-Wl,-rpath,$H3/openssl/lib" \
./configure \
  --prefix="$H3/ngtcp2" \
  --enable-lib-only \
  --with-openssl

make -j"$(nproc)"
make install

echo "[4/4] Building curl $CURL_VERSION with HTTP/3..."
cd "$SRC"

if [ ! -f "curl-$CURL_VERSION.tar.xz" ]; then
  wget "https://curl.se/download/curl-$CURL_VERSION.tar.xz"
fi

if [ ! -d "curl-$CURL_VERSION" ]; then
  tar -xf "curl-$CURL_VERSION.tar.xz"
fi

cd "$SRC/curl-$CURL_VERSION"

PKG_CONFIG_PATH="$H3/openssl/lib/pkgconfig:$H3/ngtcp2/lib/pkgconfig:$H3/nghttp3/lib/pkgconfig" \
LDFLAGS="-Wl,-rpath,$H3/openssl/lib:$H3/ngtcp2/lib:$H3/nghttp3/lib" \
./configure \
  --prefix="$H3/curl" \
  --with-openssl="$H3/openssl" \
  --with-ngtcp2="$H3/ngtcp2" \
  --with-nghttp3="$H3/nghttp3" \
  --with-nghttp2

make -j"$(nproc)"
make install

echo
echo "Done."
echo "Test with:"
echo "$H3/curl/bin/curl -V"
