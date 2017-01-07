working_directory = File.join(File.dirname(__FILE__))
$LOAD_PATH << working_directory unless $LOAD_PATH.include?(working_directory)
Dir[File.join(working_directory, '*.jar')].each do |jar|
  require jar
end

%w(JoonsRenderer JRFiller JRImagePanel JRRecorder JRStatics).each do |name|
  java_import format("joons.%s", name)
end


