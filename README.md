# niri-watcher

CLI tool that restore states from [niri](https://github.com/YaLTeR/niri)'s event stream, make it easy to create one-liner scripts for niri.

## Build

### non-Nix

With clang installed:

```sh
SCALANATIVE_MODE=release-full SCALANATIVE_LTO=thin ./mill nativeLink
cp out/nativeLink.dest/out /usr/local/bin/niri-watcher
niri-watcher --help
```

### Nix

```sh
nix build .
```
