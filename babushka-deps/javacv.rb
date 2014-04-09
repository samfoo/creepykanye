dep "javacv" do
  met? {
    "~/.m2/repository/javacv".p.exists? &&
    "~/.m2/repository/javacv-macosx-x86_64".p.exists? &&
    "~/.m2/repository/opencv-native".p.exists?
    "~/.m2/repository/javacpp".p.exists?
  }

  javacv_uri = "https://javacv.googlecode.com/files/javacv-0.7-bin.zip"
  javacpp_uri = "https://javacv.googlecode.com/files/javacv-0.7-cppjars.zip"

  def install_artifact jar, artifact, version
    shell "lein localrepo install #{jar} #{artifact} #{version}"
  end

  meet {
    require 'tmpdir'

    Dir.mktmpdir do |dir|
      cd dir, create: true do
        puts "Fetching javacv to #{dir}"
        shell "wget -O 'javacv.zip' #{javacv_uri}"

        puts "Fetching javacpp to #{dir}"
        shell "wget -O 'javacpp.zip' #{javacpp_uri}"

        shell "unzip javacv.zip"
        shell "unzip javacpp.zip"
      end

      install_artifact(File.join(dir, 'javacv-cppjars/opencv-2.4.8-macosx-x86_64.jar'),
                       "opencv-native",
                       "2.4.8")

      install_artifact(File.join(dir, 'javacv-bin/javacv-macosx-x86_64.jar'),
                       "javacv-macosx-x86_64",
                       "0.7")

      install_artifact(File.join(dir, 'javacv-bin/javacv.jar'),
                       "javacv",
                       "0.7")

      install_artifact(File.join(dir, 'javacv-bin/javacpp.jar'),
                       "javacpp",
                       "0.7")
    end
  }
end
