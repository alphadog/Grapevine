require 'rubygems'
require 'sinatra'
require 'json'

require File.dirname(__FILE__) + '/grapevine_ws/review'
require File.dirname(__FILE__) + '/grapevine_ws/utils/hash_ext'

before do
	halt 401 if request.params["token"] != "e3e5f11e6c9cd54fc0fce481cf10f091"
end

get '/reviews' do
	content_type 'application/json'
  {:reviews => Review.all}.to_json
end

get '/reviews/:id' do
	content_type 'application/json'
  {:reviews => [Review.find(params[:id])]}.to_json
end

post '/reviews' do
	response.status = 201 if Review.new(params.symbolize_keys.reject {|k,v| k == :token})
end

