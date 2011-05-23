#! /usr/bin/ruby
require 'rubygems'
require "cgi"
require 'zipruby'
require 'fileutils'
require 'yaml'

config = YAML.load_file("config.yaml")
OUTPUT_DIR = config[:public_folder_path] + '/' + config[:scirocco_doc_relative_path] + '/report/' 

def ore_unzip(src_path, output_path)
	output_path = (output_path + "/").sub("//", "/")
	Zip::Archive.open(src_path) do |archives|
		archives.each do |a|
			d = File.dirname(a.name)
			FileUtils.makedirs(output_path + d)
			unless a.directory?
				File.open(output_path + a.name, "w+b") do |f|
					f.print(a.read)
				end
			end
		end
	end
end


begin 
	print "Content-type: text/html\n\n"
	cgi = CGI.new
	cgi_param = cgi.params
	project_name = cgi_param['project_name'][0].read
	class_name = cgi_param['class_name'][0].read
	device_name = cgi_param['device_name'][0].read
	date = cgi_param['date'][0].read

	p "project=#{project_name}"
	p "class=#{class_name}"
	p "device=#{device_name}"
	p "date=#{date}"

	#TempfileなのかStringIOなのかを表示
	print cgi['file'].class.to_s + "<br>"

	#アップロードしたファイル名
	print cgi['file'].original_filename + "<br>"

	#ファイルサイズ
	print cgi['file'].size.to_s + "<br>"

	#プロジェクトディレクトリを作成
	project_dir = "#{OUTPUT_DIR}#{project_name}"
	unless File.exist?(project_dir)
		Dir::mkdir(project_dir)
	end
	p "project_dir_created!"

	#クラスディレクトリを作成
	class_dir = "#{project_dir}/#{class_name}"
	unless File.exist?(class_dir)
		Dir::mkdir(class_dir)
	end
	p "class_dir_created!"


	#デバイスディレクトリを作成
	device_dir = "#{class_dir}/#{device_name}"
	unless File.exist?(device_dir)
		Dir::mkdir(device_dir)
	end
	p "device_dir_created!"

	#日付ディレクトリを作成
	date_dir = "#{class_dir}/#{device_name}"
	print "output_file_path=#{date_dir}\n"
	zip_file_path = OUTPUT_DIR + cgi['file'].original_filename
  p zip_file_path
	open(zip_file_path, "w") {|fh|
		fh.binmode
		fh.write cgi['file'].read
	}
	p "date_dir_created!"
	p "Success!"

	#zipファイルを解凍
	ore_unzip(zip_file_path,date_dir)

rescue => e
	print "Error!!" + e
end
