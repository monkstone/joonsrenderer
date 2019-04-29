---
layout: page
title: About
permalink: /about/
---
JoonsRenderer is a library for Processing by [Joon Hyub Lee][joonhyublee]. It includes the sunflow java raytracer by [Christopher Kulla][fpsunflower], and the necessary janino compiler by [Arno Unkrig][Arno].  In this gem the JoonsLibrary and sunflower libraries are re-compiled (by Martin Prout) for java-8 and to use janino-3.0.12 The gem is designed to be used with [JRubyArt][jruby_art] or [propane][propane], all you need to do is `require 'joonsrenderer'` and `include_package 'joons'` to use the library.

[joonhyublee]:https://github.com/joonhyublee/joons-renderer/wiki
[fpsunflower]:https://github.com/fpsunflower/sunflow
[Arno]:http://janino-compiler.github.io/janino/
[propane]:https://ruby-processing.github.io/propane/
[jruby_art]:https://ruby-processing.github.io/JRubyArt/
