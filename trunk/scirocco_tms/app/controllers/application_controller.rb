# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  before_filter :basic_authentication
  helper :all # include all helpers, all the time
  #外部からのPOSTリクエストを有効化
  #protect_from_forgery # See ActionController::RequestForgeryProtection for details
  #ActionController::InvalidAuthenticityToken これ使える？
  # Scrub sensitive parameters from your log
  # filter_parameter_logging :password

  # テストだけ使うBasic認証
  protected
  def basic_authentication
    authenticate_or_request_with_http_basic do |user, pass|
      [user, pass] == ['sonixasia', 'sonixtech']
    end
  end
end
