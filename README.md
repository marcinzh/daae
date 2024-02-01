[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.marcinzh/daae-core_3/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.marcinzh/daae-core_3)
# Debug as an Effect (DaaE)

Tiny debugger, implemented with Algebraic Effects.

Inspired by [stepwise](https://share.unison-lang.org/@pchiusano/stepwise)
for [Unison](https://www.unison-lang.org/) language.

In [this announcement post on Twitter](https://twitter.com/pchiusano/status/1502760429466042368),
the author linked [this 3-minute video](https://www.loom.com/share/e26bd00831464241bcc5e1961840af19), showing the debugger in action.

---

This project is a reimplementation of the idea in Scala,
using [Turbolift](https://github.com/marcinzh/turbolift) effect system.

![image](img/screenshot.png)

---

You can run included demos using `scala-cli`:

1. [OriginalDemo](modules/demos/src/main/scala/demos/OriginalDemo.scala) - The original demo from [stepwise](https://share.unison-lang.org/@pchiusano/stepwise), shown in the video. Translated to Scala.
```bash
scala-cli https://raw.githubusercontent.com/marcinzh/daae/master/modules/demos/src/main/scala/demos/OriginalDemo.scala  
```


2. [TreeWalk](modules/demos/src/main/scala/demos/TreeWalk.scala) - Using multiple effects.
```bash
scala-cli https://raw.githubusercontent.com/marcinzh/daae/master/modules/demos/src/main/scala/demos/TreeWalk.scala
```
