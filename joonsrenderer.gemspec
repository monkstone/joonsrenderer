# -*- encoding: utf-8 -*-
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'joonsrenderer/version'

Gem::Specification.new do |gem|
  gem.name          = 'joonsrenderer'
  gem.version       = JoonsRenderer::VERSION
  gem.authors       = ['monkstone']
  gem.email         = ['mamba2928@yahoo.co.uk']
  gem.licenses     = %w(GPL-3.0)
  gem.description   = %q{A realistic ray tracer for propane and JRubyArt}
  gem.summary       = %q{From Sketch to Ray Traced Image}
  gem.homepage      = 'https://ruby-processing.github.io/joonsrenderer/'
  gem.files         = `git ls-files`.split($/)
  gem.files << 'lib/joonsrenderer.jar'
  gem.files << 'lib/janino-3.0.6.jar'
  gem.files << 'lib/commons-compiler-3.0.6.jar'
  gem.executables   = gem.files.grep(%r{^bin/}).map{ |f| File.basename(f) }
  gem.test_files    = gem.files.grep(%r{^(test|spec|features)/})
  gem.require_paths = ['lib']
  gem.platform      = 'java'
end
