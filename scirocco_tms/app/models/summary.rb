class Summary < ActiveRecord::Base
  belongs_to :project

  def formatted_date
    return "#{date[0..3]}/#{date[4..5]}/#{date[6..7]} #{date[8..9]}:#{date[10..11]}:#{date[12..13]}"
  end
end
