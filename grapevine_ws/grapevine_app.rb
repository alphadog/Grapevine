require 'rubygems'
require 'sinatra'
require 'json'

require File.dirname(__FILE__) + '/review'
require File.dirname(__FILE__) + '/utils/hash_ext'

get '/reviews' do
  Review.all.to_json
end

post '/reviews' do
	Review.new(params.symbolize_keys)
end

