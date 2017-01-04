require_relative 'lib/joonsrenderer/version'

def create_manifest
  title =  'Implementation-Title: joonsrenderer (java extension for joonsrenderer gem)'
  version =  format('Implementation-Version: %s', JoonsRenderer::VERSION)
  file = File.open('MANIFEST.MF', 'w') do |f|
    f.puts(title)
    f.puts(version)
    f.puts('Class-Path: janino-3.0.6.jar commons-compiler-3.0.6.jar')
  end
end

task default: [:init, :compile, :gem]

desc 'Create Manifest'
task :init do
  create_manifest
end

desc 'Build gem'
task :gem do
  sh "gem build joonsrenderer.gemspec"
end

desc 'Compile'
task :compile do
  sh "mvn package"
  sh "mv target/joonsrenderer.jar lib"
end

desc 'clean'
task :clean do
  Dir['./**/*.%w{jar gem}'].each do |path|
    puts "Deleting #{path} ..."
    File.delete(path)
  end
  FileUtils.rm_rf('./target')
end
