addCompilerPlugin(
  "org.scalameta" % "semanticdb-scalac" % "4.7.8" cross CrossVersion.full
)
scalacOptions += "-Yrangepos"
