require 'rubygems'
require 'sinatra'
require 'json'

require File.dirname(__FILE__) + '/grapevine_ws/review'
require File.dirname(__FILE__) + '/grapevine_ws/utils/hash_ext'

get '/reviews' do
	content_type 'application/json'
  Review.all.to_json
end

post '/reviews' do
	Review.new(params.symbolize_keys)
end

