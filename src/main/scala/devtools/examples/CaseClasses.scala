package devtools.examples

case class SomeTool(name: String, isBroken: Boolean, funnyNumbers: List[Int])

case class SomeToolbox(tools: List[SomeTool])
