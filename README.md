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

## Example

### Track focused window's app_id

```sh
niri-watcher windows | jq -ncr 'def filter(i): i | [.[] | select(.is_focused)] | { id: .[0].id, app_id: .[0].app_id }; foreach inputs as $item ([null, null]; [.[1], filter($item)]; if .[1] == .[0] then empty else .[1].app_id end)'
```

### Notify me when specified app closed

```sh
niri-watcher windows | jq -ncr 'def filter(i): i | [.[] | select(.app_id == "firefox")] | length; foreach inputs as $item ([null, null]; [.[1], filter($item)]; if .[1] == .[0] or .[1] > 0 then empty else "all closed" end)' --unbuffered | while IFS= read -r line; do notify-send "$line"; done
```

### Sticky floating windows

```sh
niri-watcher workspaces | jq -ncr 'def filter(i): i | [ .[] | select(.is_focused) | { id: .id, idx: .idx, output: .output } ].[0]; foreach inputs as $item ([null, null]; [.[1], filter($item)]; if .[1] == .[0] or .[1].output != .[0].output then empty else { prevId: .[0].id, currIdx: .[1].idx } end)' --unbuffered | while IFS= read -r line; do prevId=$(echo $line | jq -r '.prevId'); windows=$(niri msg -j windows | jq '[.[] | select(.is_floating and .workspace_id == '"$prevId"').id]'); echo [$line, $windows] | jq -r '.[0].currIdx as $currIdx | .[1].[] | "msg action move-window-to-workspace --window-id " + (. | tostring) + " --focus false " + ($currIdx | tostring)' --unbuffered | while IFS= read -r args; do niri $args; done; done
```
