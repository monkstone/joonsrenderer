require 'fileutils'
project 'joonsrenderer' do

  model_version '4.0.0'
  id 'joons:joonsrenderer:1.3.0'
  packaging 'jar'
  description 'joonsrenderer for propane and JRubyArt'
  organization 'ruby-processing', 'https://ruby-processing.github.io'
  { 'fpsunflower' => 'Christopher Kulla', 'geneome' => 'Eugene Reilly', 'joonhyublee' => 'Joon Hyub Lee', 'monkstone' => 'Martin Prout' }.each do |key, value|
    developer key do
      name value
      roles 'developer'
    end
  end
  license 'GPL 3', 'http://www.gnu.org/licenses/gpl-3.0-standalone.html'
  issue_management 'https://github.com/monkstone/joonsrenderer/issues', 'Github'

  properties( 'source.directory' => 'src',
  'joonsrenderer.basedir' => '${project.basedir}',
  'polyglot.dump.pom' => 'pom.xml',
  'maven.compiler.source' => '1.8',
  'project.build.sourceEncoding' => 'utf-8',
  'maven.compiler.target' => '1.8',
  'janino.version' => '3.1.2',
  'jogl.version' => '2.3.2',
  'processing.version' => '3.3.7'
  )

  jar 'org.processing:core:${processing.version}'
  jar 'org.jogamp.jogl:jogl-all:${jogl.version}'
  jar 'org.jogamp.gluegen:gluegen-rt:${jogl.version}'
  jar('org.codehaus.janino:commons-compiler:${janino.version}')
  jar('org.codehaus.janino:janino:${janino.version}')

  overrides do
    plugin :resources, '3.1.0'
    plugin :dependency, '3.1.2' do
      execute_goals( id: 'default-cli',
        artifactItems:[
        { groupId: 'org.codehaus.janino',
          artifactId: 'janino',
          version: '${janino.version}',
          type: 'jar',
          outputDirectory: '${joonsrenderer.basedir}/lib'
        },
        { groupId: 'org.codehaus.janino',
          artifactId: 'commons-compiler',
          version: '${janino.version}',
          type: 'jar',
          outputDirectory: '${joonsrenderer.basedir}/lib'
        }
      ]
      )
    end
    plugin(:compiler, '3.8.1',
           'release' => '11')
    plugin(:javadoc, '2.10.4',
           'detectOfflineLinks' => 'false',
           'links' => ['${processing.api}',
                       '${jruby.api}'])
    plugin(:jar, '3.2.0',
      'archive' => {
        'manifestFile' =>  'MANIFEST.MF'
      })
    plugin :jdeps, '3.1.2' do
      execute_goals 'jdkinternals', 'test-jdkinternals'
    end
  end

  build do
    default_goal 'package'
    source_directory '${source.directory}/main/java'
    final_name 'joonsrenderer'
  end
end
