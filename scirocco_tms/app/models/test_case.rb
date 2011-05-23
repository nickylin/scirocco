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

def get_screenshot_path(td)
  ary_img = []
  (td/:table/:tr/:td/:img).each do |img|
    ary_img.push img[:src]
  end
  return ary_img.join ","
end

def create_summary(date_dir,project_id,class_name,device,date)
  open("#{date_dir}/report.html","r") do |file|
    doc = Hpricot(file)
    td = (doc/'#test_summary'/'table'/'td')
    sum = Summary.new
		sum.project_id = project_id
		sum.class_name = class_name
    sum.device = device
    sum.date = date
    sum.pass =  td[4].inner_html
		sum.fail =  td[5].inner_html
    sum.save
  end
end

def create_test_cases(date_dir,project_id,class_name,device,date)
  open("#{date_dir}/report.html","r") do |file|
    doc = Hpricot(file)
    (doc/'#test_case'/'table'/'tr').each_with_index do |tr,i|
      if i == 0 || i%2 != 0
        next
      end
      td = tr/'td'
      tc = TestCase.new
      tc.project_id = project_id
      tc.class_name = class_name
      tc.device = device
      tc.date = date
      tc.result = (td[0]/:font).inner_html
      tc.test_name = td[1].inner_html
      tc.test_procedure = td[2].inner_html
      tc.confirmation_contents = td[3].inner_html
      tc.trace = td[4].inner_html
      tc.screen_shot_path = get_screenshot_path(td[5])
      tc.save
    end
  end
end


OUTPUT_DIR = "public/scirocco/"

class TestCase < ActiveRecord::Base
  belongs_to :project
  def self.save_test_case(params)
    upload = params[:file]
    project_name = params[:project].read
    class_name = params[:class_name].read
    device = params[:device].read
    date = params[:date].read


    project = Project.find(:first,:conditions => ["name == ?",project_name])

    if project == nil
      project = Project.new
      project.name = project_name
      project.save
    end

#    p "project_name=#{project_name}"
#    p "class=#{class_name}"
#    p "device=#{device}"
#    p "date=#{date}"

    #プロジェクトディレクトリを作成
    project_dir = "#{OUTPUT_DIR}#{project_name}"
    unless File.exist?(project_dir)
      Dir::mkdir(project_dir)
      p "mkdir:" + project_dir
      p "project_dir_created!"
    end

    #クラスディレクトリを作成
    class_dir = "#{project_dir}/#{class_name}"
    unless File.exist?(class_dir)
      Dir::mkdir(class_dir)
      p "mkdir:" + class_dir 
      p "class_dir_created!"
    end


    #デバイスディレクトリを作成
    device_dir = "#{class_dir}/#{device}"
    unless File.exist?(device_dir)
      Dir::mkdir(device_dir)
      p "device_dir_created!"
    end


    #日付ディレクトリを取得
    zip_dest_dir = "#{class_dir}/#{device}"

    #zipファイルを作成
    zip_file_path = upload.original_filename
    f=File.open(zip_file_path, "wb")
    f.write(upload.read)
    f.close
    #zipファイルを解凍
    ore_unzip(zip_file_path,zip_dest_dir)
    date_dir = "#{class_dir}/#{device}/#{date}"

    create_summary(date_dir,project.id,class_name,device,date)
    create_test_cases(date_dir,project.id,class_name,device,date)
    return "" 
  end

end
