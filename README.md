# Scala dev tools

Some tools for neovim to better scala coding xp.

# Setup

Add semanticdb compiler plugin:

```
addCompilerPlugin(
  "org.scalameta" % "semanticdb-scalac" % "4.7.8" cross CrossVersion.full
)
scalacOptions += "-Yrangepos"
```

Profit!
