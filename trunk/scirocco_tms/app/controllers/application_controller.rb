# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  before_filter :basic_authentication
  helper :all # include all helpers, all the time
  # Basic認証
  protected
  def basic_authentication
#    authenticate_or_request_with_http_basic do |user, pass|
#      [user, pass] == ['user', 'pass']
#    end
  end
end
